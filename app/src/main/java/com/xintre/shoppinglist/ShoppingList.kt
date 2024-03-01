package com.xintre.shoppinglist

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.xintre.locatotionapp.LocationUtils

data class ShoppingItem(
    val id: Int,
    var name: String,
    var quantity: String,
    var isEditing: Boolean = false,
    var address: String = ""
)

fun isNumericNotZero(toCheck: String): Boolean {
    return (Regex("[^0]\\d*").matches(toCheck))
//    return toCheck.all { char -> char.isDigit() }
}

//@Composable
//fun Clickable(
//    onClick: (() -> Unit)? = null,
//    consumeDownOnStart: Boolean = false,
//    children: @Composable() () -> Unit
//)
//Clickable(onClick = {
//    Toast.makeText(context, "You just clicked a Clickable", Toast.LENGTH_LONG)
//        .show()
//}) {
//    Text(text = "Hello!")
//}

//@Composable
//fun DisableButton(
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    content: @Composable () -> Unit
//) {
//    val isEnabled = true
//
//    Button(
//        onClick = onClick,
//        enabled = isEnabled,
//        modifier = modifier.then(
//            if (isEnabled) Modifier else Modifier.alpha(0.5F)
//        )
//    ) {
//        content()
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context: Context,
    address: String
) {
// this variable sItems mantains the state of the list of items
// in mutableStateOf(type - here I want to create an object of class ShoppingItem)
// here I store a list of ShoppingItems
    var sItems by remember { mutableStateOf(listOf<ShoppingItem>()) }
    var showDialog by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    // quantity needs to be a string because the user enters a value - a string, and I display it
    // as a string - I don't execute any multiplications and so
    var itemQuantity by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(true) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(), // it is ALWAYS the same
        // here we check what kind of permissions we want to ask for
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            ) {
// I have access to location
                locationUtils.requestLocationUpdates(viewModel = viewModel)
            } else {
                // the reason why we want permission
                val rationaleRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationaleRequired) {
                    Toast.makeText(
                        context,
                        "Location Permission is required for this feature to work",
                        Toast.LENGTH_LONG
                    ) // how long we want to display it
                        .show() // we want to display it
                } else {
                    // app can't request location again - it needs to be done manually
                    Toast.makeText(
                        context,
                        "Location Permission is required. Please enable it in the Android Settings",
                        Toast.LENGTH_LONG
                    ) // how long we want to display it
                        .show() // we want to display it
                }
            }
        })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "My Shopping List",
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp)
        )
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally), //.background(Color(0XFF4A235A)),
        ) {
            Text("Add Item")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(sItems) { item ->
                if (item.isEditing) {
                    // what happens when I click on onEditComplete - editing value is set to false
                    //modifying items inside of a list
                    ShoppingItemEditor(item = item, onEditComplete = { editedName, editedQuantity ->
                        sItems = sItems.map { it.copy(isEditing = false) }
                        // finding the id of the item that is currently being edited
                        // and storing it in editedItem
                        val editedItem = sItems.find { it.id == item.id }
                        // let - scope function - it allows to execute a code block within the context of an object
                        // then we use map - it's the best for mapping operations
                        // all in all we use let because it is a null safe operation on an object
                        // I don't have to check is the object null or not
                        // a safe call because it only will be executed if the object isn't null
                        editedItem?.let {
                            it.name = editedName
                            it.quantity = editedQuantity
                            it.address = address
                        }
                    })
                } else {
                    ShoppingListItem(item = item,
                        onEditClick = {
                            // find the item that is clicked on - the one that we want to edit
                            // and change the isEditing to true
                            sItems = sItems.map { it.copy(isEditing = it.id == item.id) }
                        },
                        onDeleteClick = {
                            sItems = sItems - item
                        })
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            // if the field IS NOT EMPTY - I create a new object
                            // if it IS EMPTY - do nothing
                            // checking if the name of the product is not empty and if the quantity
                            // is only numbers
                            if (itemName.isNotBlank()) {
                                if (isNumericNotZero(itemQuantity)) {
                                    val newItem = ShoppingItem(
                                        id = sItems.size + 1,
                                        name = itemName,
                                        quantity = itemQuantity
                                    )
                                    // adding an item to the list
                                    // the list = the list and a new item
                                    sItems = sItems + newItem
                                    // after adding an item the alert box should be hidden hence:
                                    showDialog = false
                                    // I clear the box so the name doesn't overite
                                    itemName = ""
                                    itemQuantity = ""
                                } else {
                                    // quantity is not only numbers - do nothing and make a Toast
                                    // with a request for a correct number
                                    Toast.makeText(
                                        context,
                                        "Please enter an integer that doesn't start with a zero",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Please enter the item name",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        //modifier = Modifier.background(Color(0XFF4A235A))
                    ) {
                        Text("Add")
                    }
                    Button(
                        onClick = { showDialog = false },
                        //modifier = Modifier.backgound(Color(0XFF4A235A))
                    ) {
                        Text("Cancel")
                    }
                }
            },
            title = { Text("Add Shopping Item") },
            text = {
                Column {
                    Text("Please enter the item name:", modifier = Modifier.padding(8.dp))
                    // "it" is provided by onValueChange - the value that the user has put
                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Text("Please enter the item quantity:", modifier = Modifier.padding(8.dp))
                    OutlinedTextField(
                        value = itemQuantity,
                        onValueChange = { itemQuantity = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Text("Picked address:", modifier = Modifier.padding(8.dp))
                    OutlinedTextField(
                        value = address,
                        onValueChange = { itemQuantity = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                    Button(
                        onClick = {
                            isEnabled = false
                            if (locationUtils.hasLocationPermission(context)) {
                                locationUtils.requestLocationUpdates(viewModel)
                                navController.navigate("locationscreen") {
                                    this.launchSingleTop
                                }
                            } else {
                                requestPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                    ) {
                        Text("address")
                    }
                }
            }
        )
    }
}

@Composable
fun ShoppingItemEditor(item: ShoppingItem, onEditComplete: (String, String) -> Unit) {
    var editedName by remember { mutableStateOf(item.name) }
    var editedQuantity by remember { mutableStateOf(item.quantity) }
    var isEditing by remember { mutableStateOf(item.isEditing) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            BasicTextField(
                value = editedName,
                onValueChange = { editedName = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )

            BasicTextField(
                value = editedQuantity,
                onValueChange = { editedQuantity = it },
                singleLine = true,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
            )
        }
        Button(
            onClick = {
                isEditing = false
                onEditComplete(editedName, editedQuantity)
            }
        ) {
            Text("Save")
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    // lambda functions
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(
                // I can pass whatever color I like!
                // but before passing hex number of the color i nee to use a format: 0XFF...
                border = BorderStroke(2.dp, Color(0XFF845885)),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 20.dp
                )
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Row {
                Text(
                    text = "name: ${item.name}",
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .padding(8.dp)
                )
                Text(
                    text = "qty: ${item.quantity}",
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center)
                        .padding(8.dp)
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                Text(text = item.address)
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = Color(0XFF4A235A)
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color(0XFF4A235A)
                )
            }
        }
    }
}
