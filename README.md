Бизнесовое описание

В рамках данного проекта нам с вами предстоит разработать очень простую, но при этом интересную систему по обработке и изменению пользовательских изображений (а может быть и не только их). Система будет состоять из двух основных сервисов, взаимодействие которых будет осуществляться в первую очередь посредством использования очереди сообщений. У каждого из них будет своя собственная база данных, и оба будут работать с общим S3 хранилищем.

Задание №1
В рамках данного задания нам с вами необходимо будет реализовать следующие юзер-стори:

Я, как пользователь, хочу иметь возможность загружать изображения на сервер
Я, как пользователь, хочу получать информацию о всех изображениях, загруженных мною ранее
Я, как пользователь, хочу иметь возможность скачать загруженное мною ранее изображение
Я, как пользователь, хочу иметь возможность удалить загруженное мною ранее изображение
Для этого необходимо будет выполнить следующие шаги:

1. Поднять сервлетное веб-приложение, работающее под капотом на Apahce Tomcat версии 8.0 и выше. Приложение должно быть реализовано с помощью Spring Boot 3.2.

2. Приложение должно предоставлять следующее API (описание того, что делает каждый из методов находится в поле description):

```
openapi: 3.0.0
info:
  title: Demo Image Processing API
  version: 0.0.1
servers:
  - url: <http://localhost>:${PORT:8080}/api/v1
tags:
  - name: Image Controller
    description: Базовый CRUD API для работы с картинками
paths:
  /image:
    post:
      tags:
        - Image Controller
      summary: Загрузка нового изображения в систему
      description: |
        В рамках данного метода необходимо:
        1. Провалидировать файл. Максимальный размер файла - 10Мб, поддерживаемые расширения - png, jpeg.
        1. Загрузить файл в S3 хранилище.
        1. Сохранить в БД мета-данные файла - название; размер; ИД файла в S3; ИД пользователя, которому файл принадлежит.
      operationId: uploadImage
      requestBody:
        content:
          multipart/form-data:
            schema:
              required:
                - "file"
              type: "object"
              properties:
                file:
                  type: "string"
                  format: "binary"
      responses:
        '200':
          description: Успех выполнения операции
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UploadImageResponse'
        '400':
          description: Файл не прошел валидацию
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
        '500':
          description: Непредвиденная ошибка
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
  /image/{image-id}:
    get:
      tags:
        - Image Controller
      summary: Скачивание файла по ИД
      description: |
        В рамках данного метода необходимо:
        1. Проверить, есть ли такой файл в системе.
        1. Проверить, доступен ли данный файл пользователю.
        1. Скачать файл.
      operationId: downloadImage
      parameters:
        - name: image-id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Успех выполнения операции
          content:
            '*/*':
              schema:
                type: string
                format: binary
        '404':
          description: Файл не найден в системе или недоступен
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
        '500':
          description: Непредвиденная ошибка
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
    delete:
      tags:
        - Image Controller
      summary: Удаление файла по ИД
      description: |
        В рамках данного метода необходимо:
        1. Проверить, есть ли такой файл в системе.
        1. Проверить, доступен ли данный файл пользователю.
        1. Удалить файл.
      operationId: deleteImage
      parameters:
        - name: image-id
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: Успех выполнения операции
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
        '404':
          description: Файл не найден в системе или недоступен
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
        '500':
          description: Непредвиденная ошибка
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
  /images:
    get:
      tags:
        - Image Controller
      summary: Получение списка изображений, которые доступны пользователю
      description: |
        В рамках данного метода необходимо:
        1. Получить мета-информацию о всех изображениях, которые доступны пользователю
      operationId: getImages
      responses:
        '200':
          description: Успех выполнения операции
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GetImagesResponse'
        '500':
          description: Непредвиденная ошибка
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UiSuccessContainer'
components:
  schemas:
    UiSuccessContainer:
      required:
        - success
      type: object
      properties:
        success:
          type: boolean
          description: Признак успеха
        message:
          type: string
          description: Сообщение об ошибке
    UploadImageResponse:
      required:
        - imageId
      type: object
      properties:
        imageId:
          type: string
          format: uuid
          description: ИД файла
    GetImagesResponse:
      required:
        - images
      type: object
      properties:
        images:
          type: array
          description: Список изображений
          items:
            $ref: '#/components/schemas/Image'
    Image:
      required:
        - filename
        - size
      type: object
      properties:
        imageId:
          type: string
          format: uuid
          description: ИД файла
        filename:
          type: string
          description: Название изображения
        size:
          type: integer
          format: int32
          description: Размер файла в байтах
```
          
В проекте также должен быть доступен Swagger для вашего API. Для этого понадобится зависимость springdoc.

3. Все эндпойнты должны быть доступны только авторизованным в системе пользователям. Данную логику можно скопировать из предыдущей домашней работы. Однако необходимо сделать так, чтобы картинку мог просматривать только тот пользователь, который ее загрузил.

4. Для работы методов вам так же понадобятся два хранилища данных:

База данных для хранения информации о метаданных файла. На ваше усмотрение, может быть как реляционная БД, так и NoSQL. Но решение нужно обосновать. По умолчанию будет PostgreSQL.

Хранилище самих файлов. Тут два варианта:
* Поднять отдельный инстанс ранее выбранной вами БД для хранения в ней файлов в формате BLOB. Большая часть современных решений поддерживает такое из коробки, и умеет оптимальным образом работать с большими файлами. Пример реализации такого с помощью гибернейта. Пример чтения BLOB’а с помощью Spring Data JDBC.
* Вариант со звездочкой. Поднять свой собственный S3, и реализовать хранение файлов в нем. В качестве open-source решения можно использовать вот это. Пример использования AWS. Другой вариант S3 хранилища, который вы уже использовали - MinIO.

5. И, напоследок, все это нужно будет упаковать в docker-compose файл таким образом, чтобы с помощью docker-compose run можно было легко и быстро развернуть этот кусочек нашей системы.
