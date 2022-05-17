package com.goshopping.shoppinglist.presentation.viewModels


import androidx.lifecycle.*
import com.goshopping.shoppinglist.data.room.items.Item
import com.goshopping.shoppinglist.data.room.items.ItemDao
import com.goshopping.shoppinglist.data.room.mainScreen.MainItem
import com.goshopping.shoppinglist.data.room.mainScreen.MainItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainViewModel(
    private val mainItemDao: MainItemDao,
    private val itemDao: ItemDao
) : ViewModel() {

    val allMainItems: LiveData<List<MainItem>> = mainItemDao.getAllItems().asLiveData()
    var newPosition = 0


    fun getListMain(parent: Int): LiveData<List<Item>> {
        return itemDao.getAllItems(parent).asLiveData()
    }

    fun getParent(parent: Int): LiveData<List<Item>> {
        return itemDao.getItemsNotCheckFromParent(parent).asLiveData()
    }

    fun getParentMarked(parent: Int): LiveData<List<Item>> {
        return itemDao.getItemsCheckFromParent(parent).asLiveData()
    }

    fun getCategory(parent: Int): LiveData<MainItem> {
        return mainItemDao.getCategory(parent).asLiveData()
    }

    suspend fun getMainList(): List<MainItem> {
        return mainItemDao.getAllItemsSuspend()
    }


    fun setNameMainIfEmpty(parentID: Int) {
        viewModelScope.launch {
            val category = mainItemDao.getCategorySuspend(parentID)
            category?.let {
                val parentName = category.parentName.trim()
                if (parentName == "") {
                    val newParentName = "List №"
                    val list = mutableListOf<String>()
                    for (i in allMainItems.value!!) {
                        if (i.parentName.contains(newParentName, true)) {
                            list.add(i.parentName)
                        }
                    }
                    val newName = "List №${ghjio(list)}"
                    updateCategory(category.copy(parentName = newName))
                }
            }
        }
    }

    fun ghjio(list: List<String>): Int {
        var int = 1
        if (list.isEmpty()) {
            return 1
        } else {
            var bool = true
            while (bool) {
                for (i in list) {
                    if (i.contains(("$int"), true)) {
                        int++
                        bool = true
                        break
                    } else {
                        bool = false
                    }
                }
            }

        }
        return int
    }

    fun addCategory(parent: Int) {
        viewModelScope.launch {
            mainItemDao.insert(
                MainItem(
                    id = parent,
                    parentName = "",
                    position = newPosition++,
                    allItems = 0,
                    checkedItems = 0
                )
            )
        }
    }

    /**
     * Обновляем имя категории
     */
    fun updateMainName(main: MainItem, name: String) {
        viewModelScope.launch {
            mainItemDao.update(main.copy(parentName = name))
        }
    }

    /**
     * Функция для заполнения полей покупки в БД
     */
    private fun newItem(
        itemName: String,
        itemCheck: Boolean,
        position: Int,
        parent: Int
    ): Item {
        return Item(
            itemName = itemName,
            itemCheck = itemCheck,
            parent = parent,
            position = position
        )
    }

    /**
     * Проверяет, есть ли пустые элементы
     */
    fun haveEmptyItems(parentID: Int) : Boolean {
        var boolean = true
            val itemFromParentID = itemDao.getAllSuspend(parentID)
            for (i in itemFromParentID) {
                if (i.itemName.trim() == "") {
                    boolean = false
                }
            }
        return boolean
    }


    /**
     * Добавляем новую покупку
     */
    fun addNewItem(parentID: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val main = mainItemDao.getCategorySuspend(parentID)
           // if (haveEmptyItems(parentID)){
            itemDao.insert(
                newItem(
                    "",
                    false,
                    position = position,
                    parent = parentID
                )
            )
            updateCategory(
                main!!.copy(allItems = main.allItems + 1)
            )
       //}
        }
    }

    /**
     * Удаляем покупку по нажатию на крестик
     */
    fun deleteItem(item: Item, main: MainItem?, isChecked: Boolean) {
        val new = if (isChecked) {
            main!!.copy(allItems = main.allItems - 1, checkedItems = main.checkedItems - 1)
        } else {
            main!!.copy(allItems = main.allItems - 1)
        }
        updateCategory(new)
        viewModelScope.launch {
            itemDao.delete(item)
            updateCategory(new)
        }
    }


    private fun updateCategory(item: MainItem) {
        viewModelScope.launch {
            mainItemDao.update(item)
        }
    }

    /**
     * Удаляем всю категорию
     */
    fun deleteItemMain(main: MainItem) {
        viewModelScope.launch {
            itemDao.deleteItemFromCategory(main.id)
            mainItemDao.delete(main)

        }
    }

    /**
     * Сохраняем позицию из RecyclerView в БД
     */
    fun movedItems(mutableList: List<Item>) {
        mutableList.forEachIndexed { index, item ->
            viewModelScope.launch {
                itemDao.update(
                    item.copy(
                        position = index
                    )
                )
            }
        }
    }

    /**
     * Обновляем покупку, если изменилось имя или покупка совершена
     */

    fun updateItem1(item: Item, addOrRemove: Boolean = true, mainItemID: Int = 0) {
        viewModelScope.launch {
            itemDao.update(item)
            val main = mainItemDao.getCategorySuspend(mainItemID)
            main?.let {
                updateCategory(
                    it.copy(
                        checkedItems = if (addOrRemove) {
                            main.checkedItems + 1
                        } else {
                            main.checkedItems - 1
                        }
                    )
                )
            }
        }
    }

    /**
     * Во время открытия главного фрагмента, просим предоставить нам список всех покупок и
     * извлекаем из каждой покупки ее категорию, затем запрашиваем уже имеющийся список категорий.
     * После этого сравниваем два списка и проверям, все ли категории вынесены в отдельную БД,
     * и если не все, то создаем новую категорию.
     */
    fun createMainItemsList() {
        //список категорий из полей БД покупок
        val listShopCategory: MutableList<Int> = mutableListOf()
        val all = itemDao.getAll().asLiveData().value
        if (!all.isNullOrEmpty()) {
            for (i in all) {
                if (!listShopCategory.contains(i.parent)) {
                    listShopCategory.add(i.parent)
                }
            }
        }
        //список категорий из БД категорий
        val listMain: MutableList<Int> = mutableListOf()
        if (!allMainItems.value.isNullOrEmpty()) {
            for (i in allMainItems.value!!) {
                if (!listMain.contains(i.id)) {
                    listMain.add(i.id)
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val mainItemDao: MainItemDao,
    private val itemDao: ItemDao
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(mainItemDao, itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}