package com.example.mainapplication.utils

class TaskOutput {
    // Indicates result of parsing server response
    var parseResult: ParseResults? = null

    // Indicates result of background task
    var taskResult: TaskResults? = null

    // Error caused unsuccessful result
    var taskError: Throwable? = null
}