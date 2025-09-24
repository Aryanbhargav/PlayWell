package com.video.playwell

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.video.playwell.ui.theme.PlaywellTheme

@UnstableApi
class MainActivity : ComponentActivity() {
    private val MANIFEST_URL = "https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd"
    private val LICENSE_URL  = "https://cwip-shaka-proxy.appspot.com/no_auth"

    var player: ExoPlayer?= null
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlaywellTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    PlayerScreen(
                       MANIFEST_URL,
                        LICENSE_URL,
                        onReleasePlayer = {releasePlayer()}

                    )
                }
            }
        }
    }


    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }
}
