import numpy as np
import tensorflow as tf
import cv2 
import pathlib
import matplotlib.pyplot as plt

# Get input and output tensors.
interpreter = tf.lite.Interpreter(model_path="/home/joaco/Documentos/AndroidStudioProjects/dog-breed-camera/machine learning/model_without_metadata/DogBreedModel.tflite")

input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

interpreter.allocate_tensors()

labels_file = open('model_without_metadata/labels.txt', 'r')
labels = labels_file.read().splitlines()

for file in pathlib.Path("/home/joaco/Documentos/AndroidStudioProjects/dog-breed-camera/machine learning/test images/").iterdir():
    print("-----------------------------------------------------------")
    # read and resize the image
    img = cv2.imread(r"{}".format(file.resolve()))

    new_img = cv2.resize(img, (224, 224)).astype(np.float32)
    new_img = new_img / 255.

    # input_details[0]['index'] = the index which accepts the input
    interpreter.set_tensor(input_details[0]['index'], [new_img])
    
    # run the inference
    interpreter.invoke()
    
    # output_details[0]['index'] = the index which provides the input
    output_data = interpreter.get_tensor(output_details[0]['index'])
    final_data = output_data[0]

    #print("For file {}, the output is {}".format(file.stem, dog_breed))
    result = np.argpartition(final_data, -5)[-5:]

    index = np.argmax(final_data)
    dog_breed = labels[index]
    print(dog_breed)
  #  for i in range(5):
    # index = np.argmax(final_data)
   #     dog_breed = labels[result[i]]
    #    score = float(final_data[result[i]]) *100
    
     #   print("for breed {}, score = {}".format(dog_breed, score))
   # print(np.around(final_data, 3))


labels_file.close()
