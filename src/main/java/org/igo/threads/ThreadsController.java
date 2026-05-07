/// Автор: Игонин В.Ю.

package org.igo.threads;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

/**
 * Контроллер для демонстрации работы с потоками:
 * - Запуск тяжёлых вычислений в основном потоке
 * - Запуск вычислений в отдельном потоке
 * - Отображение прогресса
 * - Возможность остановки вычислений
 */
public class ThreadsController
{
    @FXML private TextField stepsField;      // поле для ввода количества элементов массива в миллионах
    @FXML private Button mainBtn;            // Кнопка Запуск в основном потоке
    @FXML private Button separateBtn;        // Кнопка Запуск в отдельном потоке
    @FXML private ProgressBar progressBar;   // Прогресс-бар
    @FXML private Label statusLabel;         // Метка с текущим статусом
    @FXML private Button cancelBtn;          // Кнопка Остановить вычисления
    @FXML private Label resultLabel;         // Метка для отображения результата
    // Текущая выполняемая фоновая задача (null, если задача не запущена)
    private ArraySumTask currentTask = null;

    /**
     * Инициализация
     */
    @FXML
    private void initialize()
    {
        cancelBtn.setDisable(true); // В начале работы кнопка остановки должна быть отключена
    }

    /**
     * Обработчик нажатия кнопки запуск в основном потоке
     * В этом режиме интерфейс полностью блокируется на время вычислений
     */
    @FXML
    private void onMainThreadBtn()
    {
        disableButtonsDuringMainTask(); // Отключаем кнопки, чтобы пользователь не мог запустить несколько задач одновременно
        statusLabel.setText("выполняется в основном потоке...");
        progressBar.setProgress(0);
        resultLabel.setText("результат: -");
        long startTime = System.currentTimeMillis();
        int size = getArraySize();
        double[] array = WorkArr.createRandomArray(size);
        double sum = WorkArr.calculateSum(array);
        long duration = System.currentTimeMillis() - startTime;
        statusLabel.setText("завершено в основном потоке (" + duration + " мс)");
        resultLabel.setText("результат: сумма = " + String.format("%,.2f", sum));
        progressBar.setProgress(1.0);
        enableButtonsAfterMainTask(); // Включаем кнопки обратно
    }

    /**
     * Обработчик нажатия кнопки запуск в отдельном потоке
     * Вычисления выполняются в фоне, интерфейс остаётся доступным
     */
    @FXML
    private void onSeparateThreadBtn()
    {
        if (currentTask != null && currentTask.isRunning()) return; // Защита от повторного запуска, если задача уже выполняется
        disableButtonsForBackgroundTask(); // Подготавливаем интерфейс к запуску фоновой задачи
        statusLabel.setText("запуск обработки массива...");
        progressBar.setProgress(0);
        resultLabel.setText("результат: -");
        currentTask = new ArraySumTask(getArraySize()); // Создаём задачу
        // Привязываем свойства Task к элементам интерфейса
        // обновление будет происходить автоматически в JavaFX потоке
        progressBar.progressProperty().bind(currentTask.progressProperty());
        statusLabel.textProperty().bind(currentTask.messageProperty());
        // Назначаем обработчики завершения задачи
        currentTask.setOnSucceeded(e -> onTaskSucceeded());
        currentTask.setOnCancelled(e -> onTaskCancelled());
        currentTask.setOnFailed(e -> onTaskFailed());
        new Thread(currentTask, "ArraySum").start(); // Запускаем задачу в отдельном потоке
    }

    /**
     * Вызывается автоматически, когда задача успешно завершилась
     */
    private void onTaskSucceeded()
    {
        double result = currentTask.getValue();
        cleanupAfterTask();
        progressBar.setProgress(1.0);
        statusLabel.setText("успешно завершено в отдельном потоке");
        resultLabel.setText("результат: сумма = " + String.format("%.2f", result));
    }

    /**
     * Вызывается автоматически, когда пользователь нажал Остановить
     */
    private void onTaskCancelled()
    {
        cleanupAfterTask();
        progressBar.setProgress(0);
        statusLabel.setText("вычисления остановлены пользователем");
        resultLabel.setText("результат: остановлено");
    }

    /**
     * Вызывается автоматически при возникновении ошибки в задаче
     */
    private void onTaskFailed()
    {
        cleanupAfterTask();
        statusLabel.setText("ошибка при выполнении вычислений");
        Throwable ex = currentTask.getException();
        if (ex != null)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Обработчик нажатия кнопки остановить вычисления
     */
    @FXML
    private void onCancel()
    {
        if (currentTask != null && currentTask.isRunning())
        {
            currentTask.cancel(true);   // Отправляем запрос на отмену задачи
            cancelBtn.setDisable(true); // Сразу блокируем кнопку, чтобы избежать повторных нажатий
        }
    }

    /**
     * Подготавливает интерфейс перед запуском фоновой задачи
     */
    private void disableButtonsForBackgroundTask()
    {
        mainBtn.setDisable(true);
        separateBtn.setDisable(true);
        cancelBtn.setDisable(false); // разрешаем остановку
    }

    /**
     * Получение размера массива в миллионах из поля
     * @return введенный размер массива в миллионах. При ошибке возвращает 50000000
     */
    private int getArraySize()
    {
        try
        {
            int millions = Integer.parseInt(stepsField.getText().trim());
            return millions*1000000;
        }
        catch (Exception e)
        {
            return 50000000;
        }
    }

    /**
     * Очищает ресурсы и возвращает интерфейс в исходное состояние после завершения задачи
     */
    private void cleanupAfterTask()
    {
        // Убираем привязки, чтобы можно было вручную менять значения
        progressBar.progressProperty().unbind();
        statusLabel.textProperty().unbind();
        // Включаем кнопки запуска обратно
        mainBtn.setDisable(false);
        separateBtn.setDisable(false);
        cancelBtn.setDisable(true);
        currentTask = null; // Сбрасываем ссылку на задачу
    }

    /**
     * Отключает кнопки запуска при выполнении задачи в основном потоке
     */
    private void disableButtonsDuringMainTask()
    {
        mainBtn.setDisable(true);
        separateBtn.setDisable(true);
        cancelBtn.setDisable(true);
    }

    /**
     * Включает кнопки после завершения задачи в основном потоке
     */
    private void enableButtonsAfterMainTask()
    {
        mainBtn.setDisable(false);
        separateBtn.setDisable(false);
    }
}
