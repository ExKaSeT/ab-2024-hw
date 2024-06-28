Мы продолжаем развивать наше с вами приложение, и на очереди у нас с вами — асинхронные интеграции!

Задача на эту домашнюю работу относительна проста — поднять небольшой кластер кафки, сконфигурировать его и начать взаимодействовать с ним.

### 0. Описание flow
В проекте очередь сообщений будет использована в роли так называемой шины событий. Флоу обработки пользовательских запросов будет следующий:

Пользователь создает запрос на модификацию изображения, явно указывая порядок применения фильтров. Пока что мы их захардкодим, однако в будущем мы реализуем некоторый аналог service-discovery для наших обработчиков картинок.

Мы сохраняем пользовательский запрос и отправляем в шину событие о создании подобного запроса, а пользователю отдаем уникальный ИД запроса, по которому он в последствии сможет получить итоговую картинку. При этом, АПИ сохранит данный запрос в БД в статусе WIP.

Каждый из обработчиков считывает событие, и проверяет — может ли он обработать первый из оставшихся фильтров.

Если да — то он выполняет обработку и отправляет в шину новое событие о необходимости модифицировать уже обработанное им изображение.

Так повторяется до тех пор, пока фильтров для применения у нас не останется.

Как только все фильтры будут применены, каждый (или вообще отдельный) из обработчиков отправит в шину событие о готовности изображения с уникальным ИД итоговой картинки.

Данное сообщение считает АПИ и обновит статус у пользовательского запроса на DONE и проставит ИД итогового изображения, после чего пользователь сможет получить его по идентификатору из п. 2.

### 1. Доработка API
Во-первых, нам нужно дать нашим пользователям возможность указывать, какие бы эффекты они хотели бы применить к конкретному изображению. И получать изображение по ИД пользовательского запроса. Для этого необходимо доработать АПИ следующим образом:
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
  - name: Image Filters Controller
    description: Базовый CRUD API для работы с пользовательскими запросами на редактирование картинок
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
  /image/{image-id}/filters/apply:
    post:
      tags:
        - Image Filters Controller
      summary: Применение указанных фильтров к изображению
      description: |
        В рамках данного метода необходимо:
        1. Проверить, есть ли у пользователя доступ к файлу
        1. Сохранить в БД новый запрос на изменение файла:
            1. статус = WIP
            2. ИД оригинальной картинки = ИД оригинального файла
            3. ИД измененной картинки = null
            4. ИД запроса = уникальный ИД запроса в системе
        1. Отправить в Kafka событие о создании запроса
        1. Убедиться, что шаг 3 выполнен успешно, в противном случае выполнить повторную попытку
        1. Вернуть пользователю ИД его запроса
      operationId: applyImageFilters
      parameters:
        - name: image-id
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - name: filters
          in: query
          required: true
          schema:
            type: array
            items:
              type: string
              enum:
                # Приведенные ниже фильтры представлены в ознакомительных целях,
                # тут вы можете вставить все, что душе угодно
                - REVERS_COLORS
                - CROP
                - REMOVE_BACKGROUND
                - OTHER
      responses:
        '200':
          description: Успех выполнения операции
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ApplyImageFiltersResponse'
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
  /image/{image-id}/filters/{request-id}:
    get:
      tags:
        - Image Filters Controller
      summary: Получение ИД измененного файла по ИД запроса
      description: |
        В рамках данного метода необходимо найти и вернуть по ИД пользовательского запроса 
        ИД соответсвующего ему файла и статус, в котором находится процесс применения фильтров.
        По ИД оригинального изображения нужно убедиться, что ИД запроса относится к нему и 
        что у пользователя есть доступ к данному изображению (оригинальному).
      operationId: getModifiedImageByRequestId
      parameters:
        - name: image-id
          in: path
          required: true
          schema:
            type: string
            format: uuid
        - name: request-id
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
                $ref: '#/components/schemas/GetModifiedImageByRequestIdResponse'
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
    ApplyImageFiltersResponse:
      required:
        - requestId
      type: object
      properties:
        requestId:
          type: string
          format: uuid
          description: ИД запроса в системе
    GetModifiedImageByRequestIdResponse:
      required:
        - imageId
        - status
      type: object
      properties:
        imageId:
          type: string
          format: uuid
          description: ИД модифицированного или оригинального файла в случае отсутствия первого
        status:
          type: string
          enum:
            - WIP
            - DONE
          description: Статус обработки файла
```
Note: добавлены были два последних метода из контроллера Image Filters Controller

### 2. Конфигурация кластера Кафки
Кластер должен состоять из трех брокеров, в качестве координатора можете использовать либо ZooKeeper, либо Kraft - тут на ваше усмотрение. Для обеспечения безопасности нужно настроить аутентификацию с помощью SASL (в документации про это есть целый раздел).

По поводу топиков. Нам понадобятся два:

* images.wip - для тех картинок, что требуют обработки

Пример тела сообщения:
```
{
  "imageId": "uuid",   // ИД изображения с которым сейчас ведется работа по данному запросу
  "requestId": "uuid", // ИД пользовательского запроса
  "filters": [         // Фильтры, которые нужно применить
    "FIRST",
    "SECOND",
    "THIRD"
  ]
}
```
* images.done - для тех картинок, что были успешно обработаны

Пример тела сообщения:
```
{
  "imageId": "uuid",  // ИД итогового изображения
  "requestId": "uuid" // ИД пользовательского запроса
}
```
По каждому топику должно быть 3 реплики, 2 из которых должны быть синхронными. При отправке сообщений в топики нужно указывать acks=all, ведь мы не хотим потерять сообщения, а при считывании нужно гарантировать идемпотентность обработки (комитить офсет только после успешной обработки сообщений, и в случае обработчиков изображений проверять, не обрабатывали ли они уже пару imageId + requestId).

### 3. Взаимодействие с Кафкой на стороне АПИ
Вам понадобятся:
* Продьсюер, который будет отправлять сообщения в топик images.wip. В продьюсере должны быть реализованы ретраи, в случае если Кафка не смогла записать к себе ваше сообщение.
* Консьюмер, который будет считывать сообщения из топика images.done и обновлять статус обработки пользовательского запроса и ИД финального изображения. Причем офсет комитить нужно только после завершения транзакции / вставки данных в БД. Так же не забудьте проставить единый [group.id](<http://group.id>) для всех ваших логически объединенных консьюмеров, чтобы они совместно работали над разбором топика.
