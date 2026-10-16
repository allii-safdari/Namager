package com.example.di

import com.example.data.repository.*
import com.example.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepository): IMovieRepository

    @Binds
    @Singleton
    abstract fun bindSeriesRepository(impl: SeriesRepository): ISeriesRepository

    @Binds
    @Singleton
    abstract fun bindSeasonsRepository(impl: SeasonsRepository): ISeasonsRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepository): ISearchRepository

    @Binds
    @Singleton
    abstract fun bindGenreRepository(impl: GenreRepository): IGenreRepository

    @Binds
    @Singleton
    abstract fun bindCountryRepository(impl: CountryRepository): ICountryRepository

    @Binds
    @Singleton
    abstract fun bindCountryPostersRepository(impl: CountryPostersRepository): ICountryPostersRepository
}
