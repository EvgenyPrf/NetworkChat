# NetworkChat
В данном проекте представлена реализация простого сетевого чата с использованием графического интерфейса на библиотеке swing.
В чате реализован многопользовательский режим, простой процесс авторизации, а также есть бот, который в ответ на команды может направить информацию о текущей дате.

Инструкция по локальному запуску:
-  Запускаем класс Server;
-  Вводим номер порта (число от 1 до 99);
-  Получаем информацию в консоли, что сервер запущен;
-  Запускаем класс ClientGuiController;
-  В появившемся окне вводим адрес сервера (localhost);
-  Далее указываем порт сервера (тот, который указывали при запуске сервера);
-  Указываем имя (если клиент с данным именем уже авторизован, потребуется ввести уникальное имя);
-  Далее получаем информацию о том, что соединение установлено;
-  Далее тестируем реализацию бота;
-  Запускаем класс BotClient;
-  В консоли вводим адрес сервера (localhost);
-  Далее в консоли указываем порт сервера (тот, который указывали при запуске сервера);
-  При успешном подключении, в графическом окне чата, получаем приветствие от бота. 


В будущем планирую добавить следующее:
-  Подключение к БД;
-  Поддержку приватных сообщений (когда сообщение отправляется не всем, а какому-то конкретному участнику);
-  Расширить возможности бота, попробовать научить его отвечать на простейшие вопросы;
-  Добавить возможность пересылки файлов между пользователями.
