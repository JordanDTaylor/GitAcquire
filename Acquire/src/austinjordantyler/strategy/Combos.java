package austinjordantyler.strategy;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Combos {
    public static <T> ArrayList<ArrayList<T>> GenerateCombinations(T[] arr, int size) {
        return GenerateCombinations(arr)
                .stream()
                .filter(l -> l.size() == size)
                .collect(Collectors.toCollection(ArrayList<ArrayList<T>>::new));
    }

    public static <T> ArrayList<ArrayList<T>> GenerateCombinations(T[] arr) {
        ArrayList<ArrayList<T>> combinations = new ArrayList<>();
        int length = arr.length;

        for (int i = 0; i < (1 << length); ++i) {
            ArrayList<T> combination = new ArrayList<>();
            int count;

            for (count = 0; count < length; ++count) {
                if ((i & 1 << count) > 0)
                    combination.add(arr[count]);
            }

            if (count > 0 && combination.size() > 0)
                combinations.add(combination);
        }
        return combinations;
    }
}
