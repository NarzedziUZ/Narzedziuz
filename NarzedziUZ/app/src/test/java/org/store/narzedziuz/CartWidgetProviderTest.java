package org.store.narzedziuz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.store.narzedziuz.callbacks.OnCartLoaded;
import org.store.narzedziuz.models.CartItem;
import org.store.narzedziuz.repositories.CartRepository;
import org.store.narzedziuz.widgets.CartWidgetProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CartWidgetProviderTest {

    @Mock
    Context context;

    @Mock
    AppWidgetManager appWidgetManager;

    @Mock
    FirebaseAuth firebaseAuth;

    @Mock
    FirebaseUser firebaseUser;

    @Mock
    CartRepository cartRepository;

    private MockedStatic<FirebaseAuth> firebaseAuthMock;
    private MockedStatic<CartRepository> cartRepositoryMock;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        firebaseAuthMock = mockStatic(FirebaseAuth.class);
        cartRepositoryMock = mockStatic(CartRepository.class);

        firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
        cartRepositoryMock.when(CartRepository::getInstance).thenReturn(cartRepository);
    }
    @After
    public void tearDown() {
        firebaseAuthMock.close();
        cartRepositoryMock.close();
    }

    @Test
    public void onUpdate_userNotLoggedIn_showsDefaultMessage() {
        when(firebaseAuth.getCurrentUser()).thenReturn(null);

        int[] ids = {1};

        CartWidgetProvider provider = new CartWidgetProvider();
        provider.onUpdate(context, appWidgetManager, ids);

        verify(appWidgetManager).updateAppWidget(eq(1), any(RemoteViews.class));
    }

    @Test
    public void onUpdate_emptyCart_showsEmptyCartMessage() {
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("user123");

        doAnswer(invocation -> {
            OnCartLoaded callback = invocation.getArgument(1);
            callback.onSuccess(Collections.emptyList());
            return null;
        }).when(cartRepository).getCart(eq("user123"), any());

        int[] ids = {1};

        CartWidgetProvider provider = new CartWidgetProvider();
        provider.onUpdate(context, appWidgetManager, ids);

        verify(appWidgetManager, timeout(1000))
                .updateAppWidget(eq(1), any(RemoteViews.class));
    }

    @Test
    public void onUpdate_cartWithItems_showsItemCount() {
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("user123");

        CartItem item1 = mock(CartItem.class);
        CartItem item2 = mock(CartItem.class);

        when(item1.getQuantity()).thenReturn(2);
        when(item2.getQuantity()).thenReturn(3);

        List<CartItem> items = Arrays.asList(item1, item2);

        doAnswer(invocation -> {
            OnCartLoaded callback = invocation.getArgument(1);
            callback.onSuccess(items);
            return null;
        }).when(cartRepository).getCart(eq("user123"), any());

        int[] ids = {1};

        CartWidgetProvider provider = new CartWidgetProvider();
        provider.onUpdate(context, appWidgetManager, ids);

        verify(appWidgetManager, timeout(1000))
                .updateAppWidget(eq(1), any(RemoteViews.class));
    }

    @Test
    public void onUpdate_repositoryFailure_showsErrorMessage() {
        when(firebaseAuth.getCurrentUser()).thenReturn(firebaseUser);
        when(firebaseUser.getUid()).thenReturn("user123");

        doAnswer(invocation -> {
            OnCartLoaded callback = invocation.getArgument(1);
            callback.onFailure(new Exception("error"));
            return null;
        }).when(cartRepository).getCart(eq("user123"), any());

        int[] ids = {1};

        CartWidgetProvider provider = new CartWidgetProvider();
        provider.onUpdate(context, appWidgetManager, ids);

        verify(appWidgetManager, timeout(1000))
                .updateAppWidget(eq(1), any(RemoteViews.class));
    }
}