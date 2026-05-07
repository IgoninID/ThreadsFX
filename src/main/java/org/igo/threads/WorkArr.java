/// Автор: Игонин В.Ю.

package org.igo.threads;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Класс со статическими методами для работы с массивами.
 * Не требует создания объекта.
 */
public class WorkArr
{
    /**
     * Создаёт массив заданного размера и заполняет его случайными числами
     * @param size размер массива
     * @throws IllegalArgumentException если размер массива меньше 1
     * @return заполненный массив
     */
    public static double[] createRandomArray(int size) throws IllegalArgumentException
    {
        if (size <= 0)
        {
            throw new IllegalArgumentException("размер массива должен быть положительным");
        }
        double[] array = new double[size];
        for (int i = 0; i < size; i++)
        {
            array[i] = Math.random() * 1000;
        }
        return array;
    }

    /**
     * Вычисляет сумму всех элементов массива
     * @param array массив
     * @return сумма элементов
     */
    public static double calculateSum(double[] array)
    {
        if (array == null)
        {
            return 0.0;
        }
        double sum = 0.0;
        for (double value : array)
        {
            sum += value;
        }
        return sum;
    }

    /**
     * Создаёт массив и заполняет его случайными числами с обновлением прогресса через callback
     * @param size размер массива
     * @param onProgress callback для обновления прогресса (current, total)
     * @param onMessage callback для обновления сообщения
     * @param isCancelled проверка отмены
     * @throws IllegalArgumentException если размер массива меньше 1
     * @return заполненный массив
     */
    public static double[] createRandomArrayWithProgress
    (
            int size,
            BiConsumer<Long, Long> onProgress,
            Consumer<String> onMessage,
            java.util.function.BooleanSupplier isCancelled
    )
    throws IllegalArgumentException
    {
        if (size <= 0)
        {
            throw new IllegalArgumentException("размер массива должен быть положительным");
        }
        double[] array = new double[size];
        int step = Math.max(size / 10, 1);
        for (int i = 0; i < size; i++)
        {
            if (isCancelled.getAsBoolean())
            {
                onMessage.accept("задача отменена пользователем");
                return array;
            }
            array[i] = Math.random() * 1000;
            // обновляем каждые 5%
            if (i % step == 0 || i == size - 1)
            {
                int percent = (int) ((i * 100.0) / size / 2);
                onProgress.accept((long) i, size * 2L); // первый этап — от 0 до size
                onMessage.accept(String.format("заполнение массива: %d%%  (%d из %d)", percent, i, size));
            }
        }
        return array;
    }

    /**
     * Вычисляет сумму с обновлением прогресса через callback
     * @param array массив
     * @param onProgress callback для обновления прогресса (current, total)
     * @param onMessage callback для обновления сообщения
     * @param isCancelled проверка отмены
     * @return сумма элементов
     */
    public static double calculateSumWithProgress
    (
            double[] array,
            BiConsumer<Long, Long> onProgress,
            Consumer<String> onMessage,
            java.util.function.BooleanSupplier isCancelled
    )
    {
        if (array == null) return 0.0;
        double sum = 0.0;
        int size = array.length;
        int step = Math.max(size / 10, 1);
        for (int i = 0; i < size; i++)
        {
            if (isCancelled.getAsBoolean())
            {
                onMessage.accept("задача отменена пользователем");
                return sum;
            }
            sum += array[i];
            // обновляем каждые 5%
            if (i % step == 0 || i == size - 1)
            {
                int percent = 50 + (int) ((i * 100.0) / size / 2);
                long done = size + i;
                onProgress.accept(done, size * 2L);
                onMessage.accept(String.format("подсчёт суммы: %d%%  (%d из %d)", percent, i, size));
            }
        }
        return sum;
    }
}
