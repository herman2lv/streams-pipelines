package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Collecting {

    public int sum(final IntStream intStream) {
        return intStream.sum();
    }

    public int production(final IntStream intStream) {
        return intStream.reduce((i1, i2) -> i1 * i2).orElse(0);
    }

    public int oddSum(final IntStream intStream) {
        return intStream.filter(this::isOdd).sum();
    }

    private boolean isOdd(final int number) {
        return (number ^ (number + 1)) != 1;
    }

    public Map<Integer, Integer> sumByRemainder(final int i, final IntStream intStream) {
        return intStream.boxed()
                .collect(Collectors.groupingBy(n -> n % i,
                        Collectors.summingInt(n -> n)));
    }

    public Map<Person, Double> totalScores(final Stream<CourseResult> courseResults) {
        List<CourseResult> cr = courseResults.collect(Collectors.toList());
        return cr.stream()
                .collect(Collectors.toMap(CourseResult::getPerson,
                        r -> r.getTaskResults()
                                     .values().stream()
                                     .mapToInt(v -> v).sum() / (double) getNumberOfTasks(cr)));
    }

    private int getNumberOfTasks(final List<CourseResult> courseResults) {
        return (int) courseResults.stream()
                .flatMap(r -> r.getTaskResults().keySet().stream())
                .distinct()
                .count();
    }

    private int getNumberOfStudents(final List<CourseResult> courseResults) {
        return (int) courseResults.stream()
                .map(CourseResult::getPerson)
                .distinct()
                .count();
    }

    public double averageTotalScore(final Stream<CourseResult> courseResults) {
        List<CourseResult> cr = courseResults.collect(Collectors.toList());
        int numberOfAllResults = getNumberOfStudents(cr) * getNumberOfTasks(cr);
        return cr.stream().map(CourseResult::getTaskResults)
                       .flatMapToDouble(tr -> tr.values().stream().mapToDouble(i -> i))
                       .sum() / numberOfAllResults;
    }

    public Map<String, Double> averageScoresPerTask(final Stream<CourseResult> courseResults) {
        List<CourseResult> cr = courseResults.collect(Collectors.toList());
        return cr.stream().flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.summingDouble(e -> e.getValue()
                                                      / (double) getNumberOfStudents(cr))));
    }

    public Map<Person, String> defineMarks(final Stream<CourseResult> courseResults) {
        List<CourseResult> cr = courseResults.collect(Collectors.toList());
        return cr.stream()
                .collect(Collectors.toMap(CourseResult::getPerson, r -> {
                    double avgScore = r.getTaskResults()
                                              .values().stream()
                                              .mapToDouble(v -> v)
                                              .sum() / (double) getNumberOfTasks(cr);
                    return defineMark(avgScore);
                }));
    }

    String defineMark(final double score) {
        if (score > 90) {
            return "A";
        } else if (score >= 83) {
            return "B";
        } else if (score >= 75) {
            return "C";
        } else if (score >= 68) {
            return "D";
        } else if (score >= 60) {
            return "E";
        } else {
            return "F";
        }
    }

    public String easiestTask(final Stream<CourseResult> courseResults) {
        List<CourseResult> cr = courseResults.collect(Collectors.toList());
        return cr.stream().flatMap(r -> r.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No any results");
    }

    public Collector<CourseResult, FormattedTable, String> printableStringCollector() {
        return new Collector<>() {
            @Override
            public Supplier<FormattedTable> supplier() {
                return FormattedTable::new;
            }

            @Override
            public BiConsumer<FormattedTable, CourseResult> accumulator() {
                return FormattedTable::addCourseResult;
            }

            @Override
            public BinaryOperator<FormattedTable> combiner() {
                return null;
            }

            @Override
            public Function<FormattedTable, String> finisher() {
                return formattedTable -> {
                    StringBuilder builder = new StringBuilder();
                    formattedTable.createTable(builder);
                    return builder.toString();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
    }
}
