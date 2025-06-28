package com.dj.djvideomerger

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.dj.djvideomerger.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val listUri: ArrayList<Uri> = arrayListOf()

    private val player by lazy {
        ExoPlayer.Builder(this@MainActivity).build()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.playerView.player = player

        binding.btnVideoSelect.setOnClickListener { selectMultipleVideos() }
        binding.btnMergeVideo.setOnClickListener { processVideos(uris = listUri) }
    }


    private val pickMultipleVideosLauncher = registerForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            println(uris)
            listUri.addAll(uris)
        }
    }

    private fun selectMultipleVideos() {
        pickMultipleVideosLauncher.launch(arrayOf("video/*"))
    }

    @OptIn(UnstableApi::class)
    fun processVideos(uris: List<Uri>) {
        if (uris.isEmpty()) {
            return@processVideos
        }

        val context = this

        // Determine segment length
        val totalDurationMs = 2 * 60 * 1000L // 5 minutes in ms
        val perVideoDurationMs = totalDurationMs / uris.size

        val lengths = uris.map { uri ->
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                val durationStr =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationStr?.toLongOrNull() ?: 0L
            } catch (e: Exception) {
                Log.e("MetadataRetriever", "Error retrieving duration: ${e.message}")
                0L
            } finally {
                retriever.release()
            }
        }
        Log.d("Merged","Length Array :: $lengths")

        var sum = 0L
        lengths.forEach { it ->
            sum += it
        }
        Log.d("Merged","Sum of lengths :: $sum")

        val proportion = lengths.map { it ->
            ((it.toFloat() / sum) * totalDurationMs).toLong()
        }
        Log.d("Merged","Proportions array :: $proportion")


        val editedItems = uris.mapIndexed { index, uri ->
            EditedMediaItem.Builder(
                MediaItem.Builder().setUri(uri).setClippingConfiguration(
                    ClippingConfiguration.Builder().setStartPositionMs(0)
                        .setEndPositionMs(proportion[index].toLong()).build()
                ).build()
            ).setRemoveAudio(false).build()
        }

        val videoSequence = EditedMediaItemSequence.Builder(editedItems).build()

        val composition = Composition.Builder(videoSequence).build()

        val outputFile = File(context.cacheDir, "merged_output.mp4")
        //val outputUri = Uri.fromFile(outputFile)

        val transformer = Transformer.Builder(context).setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC).experimentalSetTrimOptimizationEnabled(true)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, result: ExportResult) {
                    Toast.makeText(context, "Video merged successfully!", Toast.LENGTH_LONG).show()
                    println("SUCCESS MERGED")

                    val outputFile = File(context.cacheDir, "merged_output.mp4")
                    if (outputFile.exists()) {
                        val outputUri = Uri.fromFile(outputFile)
                        val mediaItem = MediaItem.fromUri(outputUri)
                        player.setMediaItem(mediaItem)
                        player.prepare()
                        player.play()
                    }
                }

                override fun onError(
                    composition: Composition, result: ExportResult, exception: ExportException
                ) {
                    Toast.makeText(
                        context, "Error merging video: ${exception.message}", Toast.LENGTH_LONG
                    ).show()
                    println("ERROR MERGED :: ${exception.message} ")
                }
            }).build()


        transformer.start(composition, outputFile.absolutePath)
    }

}