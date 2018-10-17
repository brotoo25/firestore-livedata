# Firestore LiveData

## What's this?

Set of Kotlin extension functions used to convert realtime updates from Firebase Firestore collections/documents into LiveData objects (Android Architecture Components).

## Download

Gradle:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {

    implementation 'com.github.brotoo25:firestore-livedata:0.0.3'
}
```

## How to use

```java

FirebaseFirestore
        .getInstance()
        .collection("users")
        .livedata()
        .observe(this, Observer<QuerySnapshot> {
                Log.d("MainActivity", "${it?.size()}")
        })
```

## Parsing results

 When passing the return class type to the 'livedata' function the default **DocumentSnapshot.toObject()** is called in order to parse the final result.
 <br><br>
 In case of the default document parser not working for your use case there is also the option to pass a parser function as an argument to handle the mapping behaviour.

 ```java
FirebaseFirestore
        .getInstance()
        .collection("users")
        .livedata(User::class.java)
        .observe(this, Observer<List<User>> {
                Log.d("MainActivity", "${it?.size()}")
        })

OR

FirebaseFirestore
        .getInstance()
        .collection("users")
        .livedata({parseUser(it)})
        .observe(this, Observer<List<User>> {
                Log.d("MainActivity", "${it?.size()}")
        })

private fun parseUser(documentSnapshot: DocumentSnapshot) : User {
    return User(name = documentSnapshot.getString("name"), email = documentSnapshot.getString("email"))
}
 ```

#### Functions available for FireStore - Collections, Documents and Queries.

## Next steps

 * Create sample app
 * Documentation
