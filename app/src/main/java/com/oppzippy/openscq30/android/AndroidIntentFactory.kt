package com.oppzippy.openscq30.android

import android.content.Context
import android.content.Intent

interface IntentFactory {
    operator fun invoke(context: Context, cls: Class<*>): Intent
}

class AndroidIntentFactory : IntentFactory {
    override fun invoke(context: Context, cls: Class<*>): Intent = Intent(context, cls)
}
