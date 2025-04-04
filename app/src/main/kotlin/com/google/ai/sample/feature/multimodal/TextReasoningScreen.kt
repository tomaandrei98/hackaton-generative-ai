/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ai.sample.feature.multimodal

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.ai.sample.GenerativeViewModelFactory
import coil.size.Precision
import com.google.ai.sample.R
import com.google.ai.sample.util.UriSaver
import kotlinx.coroutines.launch

@Composable
internal fun TextReasoningRoute(
    viewModel: TextReasoningViewModel = viewModel(factory = GenerativeViewModelFactory)
) {
    val photoReasoningUiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val imageRequestBuilder = ImageRequest.Builder(LocalContext.current)
    val imageLoader = ImageLoader.Builder(LocalContext.current).build()

    TextReasoningScreen(
        uiState = photoReasoningUiState,
        onReasonClicked = { inputText, selectedItems ->
            coroutineScope.launch {
                val bitmaps = selectedItems.mapNotNull {
                    val imageRequest = imageRequestBuilder
                        .data(it)
                        // Scale the image down to 768px for faster uploads
                        .size(size = 768)
                        .precision(Precision.EXACT)
                        .build()
                    try {
                        val result = imageLoader.execute(imageRequest)
                        if (result is SuccessResult) {
                            return@mapNotNull (result.drawable as BitmapDrawable).bitmap
                        } else {
                            return@mapNotNull null
                        }
                    } catch (e: Exception) {
                        return@mapNotNull null
                    }
                }
                viewModel.reason(inputText, bitmaps)
            }
        }
    )
}

@Composable
fun TextReasoningScreen(
    uiState: TextReasoningUiState = TextReasoningUiState.Loading,
    onReasonClicked: (String, List<Uri>) -> Unit = { _, _ -> }
) {
    var userQuestion by rememberSaveable { mutableStateOf("") }
    val imageUris = rememberSaveable(saver = UriSaver()) { mutableStateListOf() }

    val pickMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { imageUri ->
        imageUri?.let {
            imageUris.add(it)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize() // Ensures the Box takes up the full screen
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.abstract_dream), // Replace with your image
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop, // Ensures the image covers the whole screen
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize() // Make Column take the full screen
//            .padding(all = 16.dp)
//                .background(Color.Red)
                .verticalScroll(rememberScrollState()) // Scrollable content inside
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth() // Only fill width, no height constraint
            ) {
                Row(
                    modifier = Modifier.padding(top = 30.dp)
                ) {
//                    IconButton(
//                        onClick = {
//                            pickMedia.launch(
//                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                            )
//                        },
//                        modifier = Modifier
//                            .padding(all = 4.dp)
//                            .align(Alignment.CenterVertically)
//                    ) {
//                        Icon(
//                            Icons.Rounded.Add,
//                            contentDescription = stringResource(R.string.add_image),
//                        )
//                    }
                    OutlinedTextField(
                        value = userQuestion,
                        label = { Text(stringResource(R.string.reason_label)) },
                        placeholder = { Text(stringResource(R.string.reason_hint)) },
                        onValueChange = { userQuestion = it },
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .padding(all = 10.dp),
                        textStyle = TextStyle(color = Color.White.copy(alpha = 0.85f))

                    )

                }
                Row(
//                    modifier = Modifier.padding(top = 50.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (userQuestion.isNotBlank()) {
                                onReasonClicked(userQuestion, imageUris.toList())
                                userQuestion = ""
                            }
                        },
                        modifier = Modifier
                            .padding(all = 4.dp)
                            .fillMaxWidth(1f)
                            .align(Alignment.CenterVertically)
                    ) {
                        Text(stringResource(R.string.action_go))
                    }
                }
//                LazyRow(
//                    modifier = Modifier.padding(all = 8.dp)
//                ) {
//                    items(imageUris) { imageUri ->
//                        AsyncImage(
//                            model = imageUri,
//                            contentDescription = null,
//                            modifier = Modifier
//                                .padding(4.dp)
//                                .requiredSize(72.dp)
//                        )
//                    }
//                }
            }
            when (uiState) {
                TextReasoningUiState.Initial -> {
                    // Nothing is shown
                }

                TextReasoningUiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
//                        CircularProgressIndicator()
                    }
                }

                is TextReasoningUiState.Success -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(all = 16.dp)
                                .fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Person Icon",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .requiredSize(36.dp)
                                    .drawBehind {
                                        drawCircle(color = Color.White)
                                    }
                            )
                            Text(
                                text = uiState.outputText, // TODO(thatfiredev): Figure out Markdown support
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .padding(start = 16.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }

                is TextReasoningUiState.Error -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(all = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showSystemUi = true)
fun TextReasoningScreenPreview() {
    TextReasoningScreen()
}