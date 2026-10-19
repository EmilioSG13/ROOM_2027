package com.example.room2027.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE email = :email AND password = :passHash LIMIT 1")
    suspend fun login(email: String, passHash: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    suspend fun porId(id: Int): Usuario?

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuario WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Usuario?

    @Insert
    suspend fun insertar(usuario: Usuario): Long
}
