# DJVideoMerger - Kotlin Project using Media3

**DJVideoMerger** is a Kotlin-based project that demonstrates how to merge multiple video files into one, with the ability to proportionally trim and adjust the videos based on the total length of the output video. This implementation uses **Media3** for media processing, including video extraction, muxing, and trimming.

---

## 🚀 Features

- **Proportional Video Merging**: Videos are merged with each video contributing a portion based on its length relative to the total length.
- **Trimming Videos**: The videos are trimmed to fit their proportional length in the final output.
- **Built with Media3**: Utilizes Android's new **Media3** library for efficient video processing.
- **Flexible Output Length**: Define the desired total output video length, and the app adjusts the portions of each input video accordingly.

---

## 🛠️ Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/dj30101/DjVideoMerger.git
   ```
2. Checkout the **master** branch.

3. Open the project in Android Studio and sync the dependencies.

4. Dependencies: The project uses Media3 for video extraction, muxing, and processing. Ensure you have the following dependencies in your build.gradle:

   ```bash
   dependencies {
      implementation 'androidx.media3:media3-transformer:1.7.1'
      implementation 'androidx.media3:media3-exoplayer:1.7.1'
      implementation 'androidx.media3:media3-ui:1.7.1'
   } 
   ```
- `media3-transformer` is used for video transformations (like merging and trimming).
- `media3-exoplayer` is used for playback (optional if you plan to preview the output).
- `media3-ui` provides UI components if you want to integrate them in your app.

## ⚙️ How It Works

1. **Proportional Calculation**:  
   The proportion of each video is determined based on its duration relative to the total length of all input videos. For example, if one video is longer than the others, it will take up a larger portion of the final output video.

2. **Video Trimming**:  
   Each video starts from **0 seconds**, and the end portion is determined based on its proportion in the final output length. Excess parts are trimmed to fit the video proportionately into the output. This ensures that each video contributes to the final merged video without exceeding its proportional time.

3. **Merging**:  
   The final output is a single video that combines these trimmed portions from each input video. The total length of the merged video matches the user-defined output length, with each video contributing a proportional stake in the final output.
