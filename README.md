# Build with AI workshop - Google Generative AI Sample for Android (Kotlin)

This Android sample app demonstrates how to use state-of-the-art 
generative AI models (like Gemini) to build AI-powered features and applications.
We are going to explore the MultiModal feature from the SDK.

This project is a fork of the official sample in here: https://github.com/google/generative-ai-android

## Requirements

1. Follow the instructions on Google AI Studio [setup page](https://makersuite.google.com/app/apikey) to obtain an API key.
2. Add your API Key to the `local.properties` file in this format

```txt
apiKey=YOUR_API_KEY
```
3. Initialize a GenerativeModel with the `gemini-flash` AI model in the GenerativeAiViewModelFactory
````txt
 val generativeModel = GenerativeModel(
                        modelName = "gemini-1.5-flash-latest",
                        apiKey = BuildConfig.apiKey,
                        generationConfig = config
                    )
````
4. Run a prompt
Call the generativeModel in the PhotoReasoningViewModel class with the given input your image as and update the UI,
```txt
generativeModel.generateContentStream(inputContent)
   .collect { response ->
   outputContent += response.text
   _uiState.value = PhotoReasoningUiState.Success(outputContent)
   }
```

## Documentation

You can find the quick start documentation for the Android Generative AI API [here](https://ai.google.dev/tutorials/android_quickstart).
# hackaton-generative-ai
