package com.example.ui.music

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.OmniViewModel

@Composable
fun MusicScreen(viewModel: OmniViewModel) {
    var prompt by remember { mutableStateOf("") }
    var isPro by remember { mutableStateOf(false) }

    val musicUrl by viewModel.generatedMusicUrl.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Music Generation (Lyria)", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Describe the music track...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Full Track (Pro)")
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = isPro, onCheckedChange = { isPro = it })
        }

        Button(
            onClick = { viewModel.generateMusic(prompt, isPro) },
            modifier = Modifier.fillMaxWidth(),
            enabled = prompt.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Generate Music")
            }
        }

        musicUrl?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Music generated successfully", style = MaterialTheme.typography.titleMedium)
                        Text("Format: Audio clip available", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text("Note: In a real app, we would use an ExoPlayer to play the base64 audio stream.", 
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary)
        }
    }
}
