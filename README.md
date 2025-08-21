[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.warting.billy/flow/badge.png)](https://maven-badges.herokuapp.com/maven-central/se.warting.se.warting.billy/flow)

# Billy the android

Our goal is to make a modern api of BillingClient so that it is easier to use in compose world.
This library is still early beta and APIs for usages will be changes!

## How to include in your project

The library is available via MavenCentral:

```groovy
allprojects {
    repositories {
        // ...
        mavenCentral()
    }
}
```

<details>
<summary>Snapshots of the development version are available in Sonatype's snapshots repository.</summary>
<p>

[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/se.warting.billy/flow?server=https%3A%2F%2Foss.sonatype.org)](https://oss.sonatype.org/content/repositories/snapshots/se/warting/billy/flow/)

```groovy
allprojects {
    repositories {
        // ...
        maven {
            url 'https://oss.sonatype.org/content/repositories/snapshots/'
        }
    }
}
```

</p>
</details>

Add it to your module dependencies:

```
dependencies {
    implementation("se.warting.billy:flow:<latest_version>")
}
```

## How to use

All you need to do is to call collect the state of an Product and then call `buy()` when it is ready:

```
val earlyBirdProduct = Product.Subscription("early_bird")
val earlyBirdProductStatus by earlyBirdProduct.statusFlow.collectAsState(
    initial = ProductStatus.Loading(earlyBirdProduct)
)

when (val earlyBirdProduct = earlyBirdProductStatus) {
    is ProductStatus.Available -> {
        Text("Available to buy!")
        
        // Show the product price using the convenient property
        earlyBirdProduct.formattedPrice?.let { price ->
            Text("Price: $price")
        }
        
        Button(onClick = {
            // Launch buy flow - simplified API
            earlyBirdProduct.buy()
        }) {
            Text(text = "buy")
        }
    }
    is ProductStatus.Loading -> Text("Loading....")
    is ProductStatus.Unavailable -> Text("Unavailable")
    is ProductStatus.Unavailable -> Text("Unavailable")
    is ProductStatus.Owned -> Text("Owned")
}

```

For a full implementation
see: [Full sample](app/src/main/java/se/warting/sampleapp/compose/ComposeBillingScreen.kt)

## Test your apps products
If you want to load your own products in the sample app you can do so by adding your apps package
name to local.properties file like this:

```
APPLICATION_ID=your.app.id
```

## Notes

There is no need to initiate the BillingClient we are doing it for you! see: [BillingInitializer](lib/src/main/java/se/warting/billy/flow/BillingInitializer.kt)
