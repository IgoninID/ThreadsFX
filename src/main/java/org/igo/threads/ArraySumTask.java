/// Автор: Игонин В.Ю.

package org.igo.threads;

import javafx.concurrent.Task;

/**
 * Задача для выполнения тяжёлых вычислений с массивом в отдельном потоке
 */
public class ArraySumTask extends Task<Double>
{
    private final int arraySize; // размер массива

    /**
     * Конструктор для задачи
     * @param arraySize размер массива
     */
    public ArraySumTask(int arraySize)
    {
        this.arraySize = arraySize;
    }

    /**
     * Вызов задачи работы с массивом
     * @return сумму элементов массива
     * @throws IllegalArgumentException если размер массива меньше 1
     */
    @Override
    protected Double call() throws IllegalArgumentException
    {
        if (arraySize <= 0)
        {
            throw new IllegalArgumentException("размер массива должен быть больше 0");
        }
        updateMessage("начинаем обработку массива...");
        updateProgress(0, arraySize * 2L);
        // создание массива со случайными элементами с прогрессом
        double[] array = WorkArr.createRandomArrayWithProgress
            (
                arraySize,
                this::updateProgress, // передаём метод updateProgress
                this::updateMessage, // передаём метод updateMessage
                this::isCancelled // передаём проверку отмены
            );
        updateMessage("массив создан. вычисляем сумму...");
        // подсчёт суммы с прогрессом
        double sum = WorkArr.calculateSumWithProgress
                (
                    array,
                    this::updateProgress, // передаём метод updateProgress
                    this::updateMessage, // передаём метод updateMessage
                    this::isCancelled // передаём проверку отмены
                );
        updateMessage("вычисления успешно завершены");
        return sum;
    }
}
