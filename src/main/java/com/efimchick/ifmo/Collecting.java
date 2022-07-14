package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Collecting {
    public int sum(IntStream intStream) {
        return intStream.sum();
    }

    public int production(IntStream intStream) {
        return intStream.reduce(1, (accumulator, value) -> accumulator * value);
    }

    public int oddSum(IntStream intStream) {
        return intStream.filter(value -> value % 2 != 0).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int divisor, IntStream intStream) {
        return intStream.boxed()
                .collect(Collectors.groupingBy(el -> el % divisor, Collectors.summingInt(el -> el)));
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResults = courseResultStream.collect(Collectors.toList());
        Supplier<Stream<CourseResult>> streamSupplier = courseResults::stream;
        long sumOfSubjects = streamSupplier.get().flatMap(courseResult -> courseResult
                .getTaskResults().keySet().stream()).distinct().count();
        return streamSupplier.get().collect(Collectors
                .toMap(CourseResult::getPerson, person -> person.getTaskResults()
                        .values().stream().map(Integer::doubleValue)
                        .reduce(0.0, Double::sum) / sumOfSubjects));
    }

    public Double averageTotalScore(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResults = courseResultStream.collect(Collectors.toList());
        Supplier<Stream<CourseResult>> streamSupplier = courseResults::stream;
        double sumOfScores = streamSupplier.get().flatMap(value -> value.getTaskResults()
                .values().stream()).reduce(0, Integer::sum);
        double sumOfStudents = streamSupplier.get().count();
        double sumOfSubjects = streamSupplier.get().flatMap(courseResult -> courseResult
                .getTaskResults().keySet().stream()).distinct().count();
        return sumOfScores / sumOfStudents / sumOfSubjects;
    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> courseResultStream) {
        List<CourseResult> courseResults = courseResultStream.collect(Collectors.toList());
        Supplier<Stream<CourseResult>> streamSupplier = courseResults::stream;
        long sumOfStudents = streamSupplier.get().count();
        Map<String, Double> mapOfResults = streamSupplier.get().
                flatMap(value -> value.getTaskResults().entrySet().stream()).collect(Collectors
                        .groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)));
        mapOfResults.entrySet().forEach(value -> value.setValue(value.getValue() / sumOfStudents));
        return mapOfResults;
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> courseResultStream) {
        return totalScores(courseResultStream).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> fromDoubleToString(entry.getValue())));
    }

    public String fromDoubleToString(double value) {
        if (value <= 100 && value > 89) {
            return "A";
        } else if (value <= 89 && value > 82) {
            return "B";
        } else if (value <= 82 && value > 74) {
            return "C";
        } else if (value <= 74 && value > 67) {
            return "D";
        } else if (value <= 67 && value > 59) {
            return "E";
        }
        return "F";
    }

    public String easiestTask(Stream<CourseResult> courseResultStream) {
        return courseResultStream.flatMap(result -> result.getTaskResults().entrySet().stream()
        ).max(Map.Entry.comparingByValue()).stream().findFirst().orElseThrow().getKey();
    }

    public Collector<CourseResult, StringBuilder, String> printableStringCollector() {
        return new Collector<>() {
            @Override
            public Supplier<StringBuilder> supplier() {
                return StringBuilder::new;
            }

            @Override
            public BiConsumer<StringBuilder, CourseResult> accumulator() {
                return ((stringBuilder, courseResult) -> stringBuilder.
                        append(String.format("|%-15s|%-15d|%-10d|%-10d|%n-5f|%n-5s|%n"
                                , courseResult.getPerson(),
                                courseResult.getTaskResults().get("Lab 3. File Tree"),
                                courseResult.getTaskResults().get("Lab 1. Figures"),
                                courseResult.getTaskResults().get("Lab 2. War and Peace"),
                                courseResult.getTaskResults().values().stream().reduce(0, Integer::sum) / 3,
                                courseResult.getTaskResults().values().stream()
                                        .reduce(0, Integer::sum) / 3)));
            }

            @Override
            public BinaryOperator<StringBuilder> combiner() {
                return null;
            }

            @Override
            public Function<StringBuilder, String> finisher() {
                return sb -> "шапка \n" + sb.toString();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return null;
            }
        };
    }
}