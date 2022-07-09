package gini.ohadsa.servicesdemo.di

import android.app.NotificationManager
import android.content.Context
import com.squareup.picasso.Picasso
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import gini.ohadsa.servicesdemo.utils.notifications.NotificationHandler
import gini.ohadsa.servicesdemo.utils.notifications.NotificationHandlerImpl
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationsModule {

    @Binds
    abstract fun bindNotificationManager(impl: NotificationHandlerImpl): NotificationHandler
}

@Module
@InstallIn(SingletonComponent::class)
object NotificationManagerProvider {
    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}