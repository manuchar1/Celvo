package com.mtislab.celvo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mtislab.auth.presentation.register.RegisterRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
         /*   RegisterRoot(
                onLoginSuccess = {
                    // აქ მოხვდები, როცა ავტორიზაცია წარმატებით გაივლის
                    Toast.makeText(
                        this,
                        "წარმატებით შეხვედით!",
                        Toast.LENGTH_LONG
                    ).show()

                    // სამომავლოდ აქედან გადაიყვან HomeScreen-ზე
                }
            )*/
        }
    }

}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

