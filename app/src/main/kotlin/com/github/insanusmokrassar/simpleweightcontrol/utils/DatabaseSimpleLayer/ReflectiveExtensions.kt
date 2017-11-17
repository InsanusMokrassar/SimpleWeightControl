package com.github.insanusmokrassar.simpleweightcontrol.utils.DatabaseSimpleLayer

import kotlin.reflect.*
import kotlin.reflect.full.instanceParameter

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class PrimaryKey

@Target(AnnotationTarget.PROPERTY)
@MustBeDocumented
annotation class Autoincrement

/**
 * List of classes which can be primitive
 */
val nativeTypes = listOf(
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        String::class,
        Boolean::class
)

/**
 * @return Экземпляр KClass, содержащий данный KCallable объект.
 */
fun <T> KCallable<T>.intsanceKClass() : KClass<*> {
    return this.instanceParameter?.type?.classifier as KClass<*>
}

/**
 * @return true если значение параметра может быть null.
 */
fun KCallable<*>.isNullable() : Boolean {
    return this.returnType.isMarkedNullable
}

/**
 * @return Экземпляр KClass, возвращаемый KCallable.
 */
fun KCallable<*>.returnClass() : KClass<*> {
    return this.returnType.classifier as KClass<*>
}

/**
 * @return true, если возвращает некоторый примитив.
 */
fun KCallable<*>.isReturnNative() : Boolean {
    return nativeTypes.contains(this.returnClass())
}

/**
 * @return true если объект помечен аннотацией [PrimaryKey].
 */
fun KProperty<*>.isPrimaryField() : Boolean {
    return this.annotations.firstOrNull { it.annotationClass == PrimaryKey::class } != null
}

/**
 * @return true если объект помечен аннотацией [Autoincrement].
 */
fun KProperty<*>.isAutoincrement() : Boolean {
    this.annotations.forEach {
        if (it.annotationClass == Autoincrement::class) {
            return@isAutoincrement true
        }
    }
    return false
}

/**
 * @return Список полей класса.
 */
fun KClass<*>.getVariables() : List<KProperty<*>> {
    return this.memberProperties.toList()
}
