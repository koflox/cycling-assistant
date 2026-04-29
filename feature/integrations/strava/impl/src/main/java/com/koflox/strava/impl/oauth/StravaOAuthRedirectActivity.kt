package com.koflox.strava.impl.oauth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives the OAuth `cyclingassistant://koflox.github.io/strava/callback?code=...` deep link
 * after the user authorizes in Custom Tabs and forwards the code to [StravaOAuthCodeProcessor].
 *
 * ## Why a dedicated Activity (and not a Composable destination)
 *
 * The deep link arrives from an external process (Chrome Custom Tabs). Android can only route
 * such intents to AndroidManifest-registered components — `<intent-filter>` cannot target a
 * Composable or a Navigation route. Even Compose Navigation's `deepLink { ... }` ultimately
 * delivers the intent to `MainActivity` first, which would then have to forward it into the
 * graph — adding a fragile redirection step and tying the OAuth flow to whatever screen the
 * user currently has on the back stack.
 *
 * Keeping the callback in its own `singleTask` activity decouples the token exchange from
 * `MainActivity`'s state: the user can be on any screen (or the app can be backgrounded/killed)
 * when the redirect fires. This activity handles the code, finishes, and the user returns to
 * exactly where they left off — no navigation churn, no need to reason about Compose
 * back-stack restoration mid-OAuth.
 *
 * ## Why finish() must be synchronous
 *
 * The activity is declared with `Theme.NoDisplay` and `launchMode="singleTask"` in the manifest.
 * `Theme.NoDisplay` requires `finish()` to be called synchronously inside `onCreate` /
 * `onNewIntent` — if the activity becomes resumed without finishing, the platform throws
 * `IllegalStateException: Activity ... did not call finish() prior to onResume() completing`.
 *
 * Therefore the token-exchange work cannot be awaited here: we hand the code to a singleton
 * [StravaOAuthCodeProcessor] (which runs on its own application-scoped coroutine) and finish
 * immediately. This is what makes the second login attempt safe — Strava remembers the OAuth
 * session in Custom Tabs cookies and redirects back instantly, so any `lifecycleScope.launch { ... finish() }`
 * approach races against `onResume` and crashes.
 *
 * ## Why we re-launch MainActivity
 *
 * Custom Tabs runs inside our task: when Strava redirects to the deep link, this activity
 * ends up on top of Custom Tabs in the same task. Just calling `finish()` would pop us off
 * but leave Custom Tabs as the topmost activity — the user would still see the (now-blank)
 * browser until they pressed back. We use [Intent.FLAG_ACTIVITY_CLEAR_TOP] +
 * [Intent.FLAG_ACTIVITY_SINGLE_TOP] on the launcher intent to bring the existing
 * `MainActivity` back to the front (preserving Compose nav state) and destroy Custom Tabs
 * along with this redirect activity in one step.
 */
@AndroidEntryPoint
internal class StravaOAuthRedirectActivity : ComponentActivity() {

    private companion object {
        const val QUERY_PARAM_CODE = "code"
        const val QUERY_PARAM_SCOPE = "scope"
    }

    @Suppress("LateinitUsage")
    @Inject
    lateinit var codeProcessor: StravaOAuthCodeProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleCreation(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleCreation(intent)
    }

    private fun handleCreation(intent: Intent) {
        handleIntent(intent)
        returnToApp()
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data ?: return
        val code = data.getQueryParameter(QUERY_PARAM_CODE)
        if (!code.isNullOrEmpty()) {
            val grantedScope = data.getQueryParameter(QUERY_PARAM_SCOPE).orEmpty()
            codeProcessor.process(code, grantedScope)
        }
    }

    private fun returnToApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}
