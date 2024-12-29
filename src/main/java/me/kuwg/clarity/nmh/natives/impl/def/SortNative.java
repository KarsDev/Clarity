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

        for (final Object o : arr) {
            if (!(o instanceof Number)) {
                Register.throwException("Expected number array");
                return VOID;
            }
        }

        QuickSort.quickSort(arr, 0, arr.length - 1);

        return VOID;
    }

    @Override
    protected boolean applies0(final List<Object> params) {
        return params.size() == 1 && params.get(0) instanceof Object[];
    }

    public static class QuickSort {
        public static void quickSort(final Object[] arr, final int low, final int high) {
            if (low < high) {
                final int pivotIndex = partition(arr, low, high);
                quickSort(arr, low, pivotIndex - 1);
                quickSort(arr, pivotIndex + 1, high);
            }
        }

        private static int partition(final Object[] arr, final int low, final int high) {
            final Number pivot = ((Number) arr[high]);
            int i = (low - 1);

            for (int j = low; j < high; j++) {
                if (((Number) arr[j]).doubleValue() <= pivot.doubleValue()) {
                    i++;
                    swap(arr, i, j);
                }
            }

            swap(arr, i + 1, high);
            return i + 1;
        }

        private static void swap(final Object[] arr, final int i, final int j) {
            final Object temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }
}