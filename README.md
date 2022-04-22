

# MediaGallery

## Download
Add the following dependency to your gradle file
```java
implementation 'com.github.vadoliya:mediagallery:$[latest-version]'
```

or use JitPack [![](https://jitpack.io/v/vadoliya/mediagallery.svg)](https://jitpack.io/#vadoliya/mediagallery)


## Usage
The minimum API is 21. Working with API 32

```java
public void openGallery() {
 registerForActivityResult.launch(
                OpenGallery(this)
                    .setMaxCount(10)
                    .setMediaType(MediaType.IMAGE_VIDEO)
                    .setFontResource(R.font.nunito_bold)
                    .setBannerAdsId(<BannesAdsId>)
                    .setDoneText("DONE")
                    .setLoadingText("Loading...")
                    .build()
            )
}

private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(MediaConstant.MEDIA_PATHS) as ArrayList<String>
                Toast.makeText(
                    this,
                    "Select ${selectedMedia.size}  $mediaType files \n${selectedMedia}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
```

The results are returned within data Intent of **onActivityResult**. String is a simple paths for returning the string Arraylist.


## Parameters

+ **setMaxCount** limit the number of items that can be selected
+ **MediaType.IMAGE** show gallery only images
+ **MediaType.VIDEO** show gallery only videos
+ **MediaType.IMAGE_VIDEO** show gallery images and videos
+ **setFontResource** set the costom font
+ **setBannerAdsId** show Banner Ads
+ **setDoneText** set the Done Text (for Difrent Language)
+ **setLoadingText** set the Loading Text (for Difrent Language)

License
=======

    Copyright 2022 hashone

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
