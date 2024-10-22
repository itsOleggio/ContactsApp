package com.example.contactsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.contactsapp.ui.theme.ContactsAppTheme
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private val contactList = mutableStateListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем контакты из VCF файла
        loadContactsFromVcf()

        setContent {
            ContactsAppTheme {
                ContactList(contactList) { phoneNumber ->
                    dialPhoneNumber(this, phoneNumber)
                }
            }
        }
    }

    // Функция для загрузки контактов из файла VCF
    private fun loadContactsFromVcf() {
        try {
            val inputStream = resources.openRawResource(R.raw.contacts) // Открываем файл из res/raw
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            var contactName = ""
            var contactNumber = ""

            reader.use { bufferedReader ->
                while (bufferedReader.readLine().also { line = it } != null) {
                    when {
                        line!!.startsWith("FN:") -> contactName = line!!.substringAfter("FN:")
                        line!!.startsWith("TEL;CELL:") -> contactNumber = line!!.substringAfter("TEL;CELL:")
                        line!!.startsWith("END:VCARD") -> {
                            contactList.add(Contact(contactName, contactNumber.ifEmpty { " Нет номера" }))
                            contactNumber = ""
                        }
                    }
                }
            }

            Toast.makeText(this, "Найдено ${contactList.size} контактов", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при загрузке контактов: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

// Модель данных для контакта
data class Contact(val name: String, val number: String)

// Функция для открытия звонилки
fun dialPhoneNumber(context: Context, phoneNumber: String) {
    if (phoneNumber != "Нет номера") {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    }
}

@Composable
fun ContactList(contacts: List<Contact>, onContactClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        items(contacts) { contact ->
            ContactItem(contact, onContactClick)
        }
    }
}

@Composable
fun ContactItem(contact: Contact, onContactClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick(contact.number) }
            .padding(8.dp)
    ) {
        Text(
            text = "Имя: ${contact.name}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Номер телефона: ${contact.number}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
