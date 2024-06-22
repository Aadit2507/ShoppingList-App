package com.example.shoppinglistapp

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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController

data class ShoppingItems(
    val id:Int,
    var name:String,
    var quantity:Int,
    var isEditing : Boolean = false,
   var address : String = ""
)

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListApp(
    locationUtils: LocationUtils,
    viewModel: LocationViewModel,
    navController: NavController,
    context : Context,
    address: String
) {

    var sItems by remember { mutableStateOf(listOf<ShoppingItems>()) }
    var showDialog by remember { mutableStateOf(false) }
    var ItemName by remember { mutableStateOf("") }
    var ItemQuantity by remember { mutableStateOf("") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if(permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){

                locationUtils.requestLocationUpdate(viewModel = viewModel)

            }else {
                val rationalRequired = ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    context as MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

                if (rationalRequired) {
                    Toast.makeText(
                        context,
                        "Location Permission is Required for this feature to work", Toast.LENGTH_LONG
                    ).show()
                }else{
                    Toast.makeText(
                        context,
                        "Location Permission is Required. Please enable it from android settings",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Add Items")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(sItems) {
           Item ->
                if(Item.isEditing){
                    ItemEditor(Item = Item , onEditComplete = {
                        editeName , editeQuantity ->
                        sItems = sItems.map{it.copy(isEditing = false)}
                        val editeItems = sItems.find{it.id == Item.id}
                        editeItems?.let{
                            it.name = editeName
                            it.quantity = editeQuantity
                            it.address = address
                        }
                    })
                }else {
                    ShopplistItem(Item = Item, onEditClick = {
                        sItems = sItems.map{it.copy(isEditing = it.id==Item.id)}
                    },
                        onDeleteClick = {
                            sItems = sItems - Item
                        }
                        )


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
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(onClick = {
                        if(ItemName.isNotBlank()){
                            val newItem = ShoppingItems(
                                id = sItems.size+1,
                                name = ItemName,
                                quantity = ItemQuantity.toInt(),
                                address = address
                            )
                            sItems = sItems + newItem
                            showDialog = false
                            ItemName = ""
                            ItemQuantity = ""
                        }
                    }) {
                        Text(text = "Add")
                    }
                    Button(onClick = { showDialog = false }) {
                        Text(text = "Cencel",

                            )
                    }
                }
            },
            title = { Text(text = "Add Shopping Items") },
            text = {
                Column{
                    OutlinedTextField(value = ItemName ,
                        onValueChange = {ItemName = it},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp)
                    )

                    OutlinedTextField(value = ItemQuantity ,
                        onValueChange = {ItemQuantity = it},
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(9.dp)


                    )
                    Button(onClick = {
                        if(locationUtils.hasLocationPermission(context)) {
                            locationUtils.requestLocationUpdate(viewModel)
                            navController.navigate("locationscreen") {
                                this.launchSingleTop
                            }
                        } else{
                                requestPermissionLauncher.launch(arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ))
                            }
                        }
                    ){
                        Text("address")

                    }
                }
            }
        )

    }

}
@Composable
fun ItemEditor(Item : ShoppingItems , onEditComplete : (String , Int ) -> Unit){
    var editeName by remember {mutableStateOf(Item.name)}
    var editeQuantity by remember {mutableStateOf(Item.quantity.toString())}
    var isEditing by remember {mutableStateOf(Item.isEditing)}
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color.White),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
Column{
    BasicTextField(
      value = editeName,
        onValueChange = {editeName = it},
        singleLine = true,
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp)
    )

    BasicTextField(
        value = editeQuantity,
        onValueChange = {editeQuantity = it},
        singleLine = true,
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp)
    )
}
        Button(
            onClick = {
                isEditing = false
                onEditComplete(editeName , editeQuantity.toIntOrNull() ?: 1)
            }
        ){
Text(text = "Save")
        }
    }
}

@Composable
fun ShopplistItem(
    Item : ShoppingItems,
    onEditClick : () -> Unit,
    onDeleteClick : ()-> Unit,

) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .border(
                border = BorderStroke(2.dp, Color.Black),
                shape = RoundedCornerShape(20)
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(modifier = Modifier
            .weight(1f)
            .padding(8.dp)){
            Row{
                Text(text = Item.name, modifier = Modifier.padding(8.dp))
                Text(text = "Qty : ${Item.quantity}", modifier = Modifier.padding(8.dp))
            }
            Row(modifier = Modifier.fillMaxWidth()){
                Icon(imageVector = Icons.Default.LocationOn , contentDescription = null)
                Text(text = Item.address)
            }
        }



        Row(
            modifier = Modifier
                .padding(8.dp)
        ) {
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)

            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
            }

        }
    }
}
