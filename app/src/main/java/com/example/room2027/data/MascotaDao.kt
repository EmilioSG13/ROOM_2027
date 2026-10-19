package com.example.room2027.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MascotaDao {
    @Query("SELECT * FROM mascota WHERE usuarioId = :usuarioId ORDER BY id DESC")
    suspend fun listarPorUsuario(usuarioId: Int): List<Mascota>

    @Query("SELECT * FROM mascota WHERE id = :id AND usuarioId = :usuarioId LIMIT 1")
    suspend fun porId(id: Int, usuarioId: Int): Mascota?

    @Insert
    suspend fun insertar(mascota: Mascota): Long

    @Update
    suspend fun actualizar(mascota: Mascota)

    @Delete
    suspend fun eliminar(mascota: Mascota)
}
