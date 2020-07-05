import tensorflow as tf
import os
import numpy as np
import tensorflow_hub as hub
train_dir = "/home/joaco/Documentos/dog/Images"
validation_dir = "/home/joaco/Documentos/dog/Images"


BATCH_SIZE = 64
IMG_SHAPE = 224 # match image dimension to mobile net input


#prevent memorization
train_image_generator = tf.keras.preprocessing.image.ImageDataGenerator(
    rescale=1./255
    )

validation_image_generator = tf.keras.preprocessing.image.ImageDataGenerator(
    rescale=1./255)


train_data_gen = train_image_generator.flow_from_directory(batch_size=BATCH_SIZE,
                                                           directory=train_dir,
                                                           shuffle=True,
                                                           target_size=(IMG_SHAPE, IMG_SHAPE))

val_data_gen = train_image_generator.flow_from_directory(batch_size=BATCH_SIZE,
                                                           directory=validation_dir,
                                                           shuffle=False,
                                                           target_size=(IMG_SHAPE, IMG_SHAPE))

# model

labels = '\n'.join(sorted(train_data_gen.class_indices.keys()))

with open('dogslabels.txt', 'w') as f:
  f.write(labels)

# getting MobileNet
URL = "https://tfhub.dev/google/tf2-preview/mobilenet_v2/feature_vector/4"
mobile_net = hub.KerasLayer(URL, input_shape=(IMG_SHAPE, IMG_SHAPE, 3))

mobile_net.trainable = False

model = tf.keras.models.Sequential([
    mobile_net,
    tf.keras.layers.Dense(120, activation='softmax')
    ])


model.compile(optimizer='adam',
              loss='categorical_crossentropy',
              metrics=['accuracy'])

model.summary()

EPOCHS = 35

history = model.fit(train_data_gen, 
                         steps_per_epoch=len(train_data_gen), 
                         epochs=EPOCHS, 
                         validation_data=val_data_gen, 
                         validation_steps=len(val_data_gen))


saved_model_dir = 'save/fine_tuning'
tf.saved_model.save(model, saved_model_dir)

model.export(export_dir='.', with_metadata=True)

converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
tflite_model = converter.convert()

with open('dog_breed_analyzer.tflite', 'wb') as f:
  f.write(tflite_model)