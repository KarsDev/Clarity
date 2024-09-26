package me.kuwg.clarity.nmh.natives.impl.def;

import me.kuwg.clarity.library.objects.VoidObject;
import me.kuwg.clarity.nmh.natives.abstracts.DefaultNativeFunction;
import me.kuwg.clarity.register.Register;

import java.util.List;

public class SortNative extends DefaultNativeFunction<VoidObject> {

    public SortNative() {
        super("sort");
    }

    @Override
    public VoidObject call(final List<Object> params) {
        final Object[] arr = (Object[]) params.get(0);

        // Check if all elements are instances of Number
        for (final Object obj : arr) {
            if (!(obj instanceof Number)) {
                Register.throwException("Expected number array");
                return VOID;
            }
        }

        // Create a new array for sorting
        double[] numberArr = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            numberArr[i] = ((Number) arr[i]).doubleValue(); // Convert to double
        }

        QuickSort.quickSort(numberArr, 0, numberArr.length - 1);

        // Update the original array with sorted values
        for (int i = 0; i < numberArr.length; i++) {
            arr[i] = numberArr[i]; // Convert back to the original type if needed
        }

        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 && params.get(0) instanceof Object[];
    }

    public static class QuickSort {
        public static void quickSort(final double[] arr, final int low, final int high) {
            if (low < high) {
                final int pivotIndex = partition(arr, low, high);
                quickSort(arr, low, pivotIndex - 1);
                quickSort(arr, pivotIndex + 1, high);
            }
        }

        private static int partition(final double[] arr, final int low, final int high) {
            final double pivot = arr[high];
            int i = (low - 1);

            for (int j = low; j < high; j++) {
                if (arr[j] <= pivot) {
                    i++;
                    swap(arr, i, j);
                }
            }

            swap(arr, i + 1, high);
            return i + 1;
        }

        private static void swap(final double[] arr, final int i, final int j) {
            final double temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
}