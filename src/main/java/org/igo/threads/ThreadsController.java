/// Автор: Игонин В.Ю.

package org.igo.threads;

import javafx.concurrent.Task;
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
public class ThreadsController {

    @FXML private TextField stepsField;      // Поле для ввода количества шагов
    @FXML private Button mainBtn;            // Кнопка Запуск в основном потоке
    @FXML private Button separateBtn;        // Кнопка Запуск в отдельном потоке
    @FXML private ProgressBar progressBar;   // Прогресс-бар
    @FXML private Label statusLabel;         // Метка с текущим статусом
    @FXML private Button cancelBtn;          // Кнопка Остановить вычисления
    @FXML private Label resultLabel;         // Метка для отображения результата

    // Текущая выполняемая фоновая задача (null, если задача не запущена)
    private Task<Double> currentTask = null;

    /**
     * Инициализация
     */
    @FXML
    private void initialize() {
        cancelBtn.setDisable(true); // В начале работы кнопка остановки должна быть отключена
    }

    /**
     * Обработчик нажатия кнопки Запуск в основном потоке
     * В этом режиме интерфейс полностью блокируется на время вычислений
     */
    @FXML
    private void onMainThreadBtn() {

        disableButtonsDuringMainTask(); // Отключаем кнопки, чтобы пользователь не мог запустить несколько задач одновременно

        statusLabel.setText("Выполняется в основном потоке...");
        progressBar.setProgress(0);
        resultLabel.setText("Результат: -");

        int steps = parseSteps(); // Получаем количество шагов из поля
        long start = System.currentTimeMillis();
        int size = getArraySize();
        double[] array = new double[size];
        double sum = 0.0;

        for (int i = 0; i < size; i++) {
            array[i] = Math.random() * 1000;
        }

        for (int i = 0; i < size; i++) {
            sum += array[i];
        }

        long duration = System.currentTimeMillis() - start;

        statusLabel.setText("Завершено в основном потоке (" + duration + " мс)");
        resultLabel.setText("Результат: сумма = " + String.format("%.2f", sum));
        progressBar.setProgress(1.0);

        enableButtonsAfterMainTask(); // Включаем кнопки обратно
    }

    /**
     * Обработчик нажатия кнопки "Запуск в отдельном потоке"
     * Вычисления выполняются в фоне, интерфейс остаётся доступным
     */
    @FXML
    private void onSeparateThreadBtn() {
        if (currentTask != null && currentTask.isRunning()) return; // Защита от повторного запуска, если задача уже выполняется

        disableButtonsForBackgroundTask(); // Подготавливаем интерфейс к запуску фоновой задачи

        statusLabel.setText("Запуск вычислений в отдельном потоке...");
        progressBar.setProgress(0);
        resultLabel.setText("Результат: -");

        currentTask = createBackgroundTask(); // Создаём задачу

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
     * Создаёт Task для выполнения вычислений в отдельном потоке
     * @return Task
     */
    private Task<Double> createBackgroundTask() {
        return new Task<>() {
            @Override
            protected Double call() throws Exception {
                int size = getArraySize();
                double[] array = new double[size];
                double sum = 0.0;

                updateMessage("Создаем массив размером "+ size + " элементов...");
                updateProgress(0, size * 2);

                long Fillstart = System.currentTimeMillis();
                for (int i = 0; i < size; i++) {
                    if (isCancelled()) {
                        updateMessage("Задача отменена пользователем");
                        return sum;
                    }

                    array[i] = Math.random() * 1000;

                    if (i % 500000 == 0 || i == size - 1) {
                        updateProgress(i, size * 2);
                        updateMessage(String.format("Заполнение массива: %d из %d (%.1f%%", i, size, (i*100.0)/size));
                    }
                }
                long Filltime = (System.nanoTime() - Fillstart) / 1000000;


                updateMessage("Подсчет суммы элементов массива...");
                long Sumstart = System.currentTimeMillis();

                for (int i = 0; i < size; i++) { // Основной цикл вычислений
                    if (isCancelled()) { // Проверяем, не была ли задача отменена пользователем
                        updateMessage("Задача отменена пользователем");
                        return sum; // возвращаем то, что успели посчитать
                    }

                    sum += array[i]; // операция с элементом массива

                    // Обновляем прогресс и сообщение
                    if (i % 500000 == 0 || i == size - 1) {
                        int total = size + i;
                        updateProgress(total, size * 2);
                        updateMessage(String.format("Подсчет суммы: %d из %d (%.1f%%)", i, size, (i*100.0)/size));
                    }
                }
                long Sumtime = (System.nanoTime() - Sumstart) / 1000000;

                updateMessage(String.format("Вычисления успешно завершены Заполнение: %d мс, Сумма: %d мс", Filltime, Sumtime));
                return sum;
            }
        };
    }

    /**
     * Вызывается автоматически, когда задача успешно завершилась
     */
    private void onTaskSucceeded() {
        double result = currentTask.getValue();
        cleanupAfterTask();
        progressBar.setProgress(1.0);
        statusLabel.setText("успешно завершено в отдельном потоке");
        resultLabel.setText("Результат: сумма = " + String.format("%.2f", result));
    }

    /**
     * Вызывается автоматически, когда пользователь нажал Остановить
     */
    private void onTaskCancelled() {
        cleanupAfterTask();
        progressBar.setProgress(0);
        statusLabel.setText("вычисления остановлены пользователем");
        resultLabel.setText("Результат: остановлено");
    }

    /**
     * Вызывается автоматически при возникновении ошибки в задаче
     */
    private void onTaskFailed() {
        cleanupAfterTask();
        statusLabel.setText("ошибка при выполнении вычислений");
        Throwable ex = currentTask.getException();
        if (ex != null) {
            ex.printStackTrace();
        }
    }

    /**
     * Обработчик нажатия кнопки Остановить вычисления
     */
    @FXML
    private void onCancel() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);   // Отправляем запрос на отмену задачи
            cancelBtn.setDisable(true); // Сразу блокируем кнопку, чтобы избежать повторных нажатий
        }
    }

    /**
     * Подготавливает интерфейс перед запуском фоновой задачи
     */
    private void disableButtonsForBackgroundTask() {
        mainBtn.setDisable(true);
        separateBtn.setDisable(true);
        cancelBtn.setDisable(false); // разрешаем остановку
    }

    private int getArraySize() {
        try {
            int millions = Integer.parseInt(stepsField.getText().trim());
            return millions*1000000;
        } catch (Exception e) {
            return 50000000;
        }
    }

    /**
     * Очищает ресурсы и возвращает интерфейс в исходное состояние после завершения задачи
     */
    private void cleanupAfterTask() {
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
    private void disableButtonsDuringMainTask() {
        mainBtn.setDisable(true);
        separateBtn.setDisable(true);
        cancelBtn.setDisable(true);
    }

    /**
     * Включает кнопки после завершения задачи в основном потоке
     */
    private void enableButtonsAfterMainTask() {
        mainBtn.setDisable(false);
        separateBtn.setDisable(false);
    }

    /**
     * Получает количество шагов из текстового поля
     * Если введено некорректное значение — возвращает 100 по умолчанию
     * @return количество шагов из текстового поля
     */
    private int parseSteps() {
        try {
            return Integer.parseInt(stepsField.getText().trim());
        } catch (Exception e) {
            return 100;
        }
    }
}
