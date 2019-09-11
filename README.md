# Pejoy

![Image](images/banner.png)

[中文](README.zh-cn.md) ![BuildStatus](https://travis-ci.org/keep2iron/pejoy.svg?branch=master)

|      Name      |                           Version                            |       Description       |
| :------------: | :----------------------------------------------------------: | :---------------------: |
|   pejoy-core   | ![Release](https://api.bintray.com/packages/keep2iron/maven/pejoy-core/images/download.svg) | image selector core lib |
|  pejoy-fresco  | ![Release](https://api.bintray.com/packages/keep2iron/maven/pejoy-fresco/images/download.svg) |    image load engine    |
|  pejoy-glide   | ![Release](https://api.bintray.com/packages/keep2iron/maven/pejoy-fresco/images/download.svg) |    image load engine    |
| pejoy-compress | ![Release](https://api.bintray.com/packages/keep2iron/maven/pejoy-compress/images/download.svg) |     image compress      |



Pejoy is a well-designed local image and video selector for Android and base on [Matisse](https://github.com/zhihu/Matisse). You can  

- Use it in Activity or Fragment
- Select images including JPEG, PNG, GIF and videos including MPEG, MP4 
- Apply different themes, including two built-in themes and custom themes
- Different image loaders
- Define custom filter rules
- More to find out yourself

## Preview
|Mode							 |Album							 |Album Category				     |Album Preview						|
|:------------------------------:|:---------------------------------:|:--------------------------------:|--------------------------------|
|**Dracula**      |![](images/dark1.png)      | ![](images/dark2.png)   |![](images/dark3.png)        |
|**Light** |![](images/light1.png) | ![](images/light2.png) |![](images/light3.png) |
|**Custom** | ![](images/custom1.png) | ![](images/custom2.png) | ![](images/custom3.png) |

## Download

gradle:

setp1: add core lib

```groovy
dependencies {
    implementation 'io.github.keep2iron:pejoy-core:$latest_version'
	//provide imageloader engine
    implementation 'io.github.keep2iron:pejoy-engine:$latest_version'

    //optional compress image use Luban compress lib
    implementation 'io.github.keep2iron:pejoy-compress:$latest_version'
}
```

setp2:add image lib

Glide version

````groovy
dependencies {
  implementation 'com.github.bumptech.glide:glide:$latest_version'
  implementation 'io.github.keep2iron:pejoy-glide:$latest_version'
  implementation 'io.github.keep2iron:pineapple-glide:$latest_version'
}
````

Fresco version

````groovy
dependencies {
  implementation 'com.facebook.fresco:fresco:$latest_version'
  implementation 'io.github.keep2iron:pejoy-fresco:$latest_version'
  implementation 'io.github.keep2iron:pineapple-fresco:$latest_version'
}
````

optional:add compress image lib

````groovy
dependencies {
    implementation 'io.github.keep2iron:pejoy-compress:$latest_version'
}
````

## Simple usage snippet

Two usages
- [Basic](#Basic)
  Only choose images

- [Expanded](#Expanded)
  choose images and compress them 

##### Init ImageLoaderManger

Since fresco is used to load images by default,so use [Pineapple](https://github.com/keep2iron/pineapple) lib load image.

```kotlin
ImageLoaderManager.init(application)
```

##### Basic

```kotlin
Pejoy.create(this)
    .choose(MimeType.ofAll(), false)
    .maxSelectable(3)
    .countable(true)
    .originalEnable(true)
    .capture(true, enableInsertAlbum = true)
    .toObservable()
    .extractStringPath() //or extractUriPath()
    .subscribe { paths->
    }
```

##### Expanded

````kotlin
Pejoy.create(this)
    .choose(MimeType.ofAll(), false)
    .maxSelectable(3)
    .countable(true)
    .originalEnable(true)
    .capture(true, enableInsertAlbum = true)
    .toObservable()
    .weatherCompressImage(this) // when original not checked.compress will execute.
    .subscribe { paths->
    }
````

##### Capture

````kotlin
Pejoy.create(this)
    .capture()
    .originalEnable(true)
    .toObservable()
    .weatherCompressImage(requireContext())
    .subscribe {
        imageResultBuilder.append("[\n")
        	it.forEach { uri ->
        		imageResultBuilder.apply {
        		append(uri)
        		if (uri != it.last()) {
        			append("\n")
        		} else {
        			append("\n]\n")
        		}
       		}
        }
        tvImageResult.text = imageResultBuilder.toString()
        Log.d("keep2iron", it.toString() + "this : " + this.hashCode())
    }
````

## ProGuard

No need......

## License

	Copyright 2019 Keep2iron.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

