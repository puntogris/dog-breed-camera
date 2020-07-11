# dog breed camera
Android camera app that detects the breed of a dog.

## Machine Learning model

Model based in 120 breed of dogs trained over 150 epochs, includes metada and its converted to Tflite to use in android.

Used:

  * Python
  * TensorFlow
  * Keras
  * Dataset used for training : [Stanford dogs dataset](http://vision.stanford.edu/aditya86/ImageNetDogs/main.html)
  * Used transfer learning with the [MobileNet V2 model](https://tfhub.dev/google/tf2-preview/mobilenet_v2/feature_vector/4)


For making predictions with python, keep in mind the model takes 224 x 224 images with 3 RGB Channels.
```python
#here is how i resized it with cv2, you can do it how you prefer
img = cv2.imread(IMAGE_PATH)
resized_img = cv2.resize(img, (224, 224)).astype(np.float32)
final_img = new_img / 255.
output = make_prediction(final_img)
```

## Android app
Used:

  * Kotlin
  * MVVM
  * CameraX
  * Hilt DI

## Screenshots
![](https://github.com/puntogris/dog-breed-camera/blob/master/screenshots/1.png&d=44)
![](https://github.com/puntogris/dog-breed-camera/blob/master/screenshots/2.webp)
![](https://github.com/puntogris/dog-breed-camera/blob/master/screenshots/3.webp)
