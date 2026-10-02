package com.uniandesfood

import com.uniandesfood.data.repository.AuthRepository
import com.uniandesfood.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        authRepository = mock()
        whenever(authRepository.currentUser).thenReturn(MutableStateFlow(null))
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `forgotPassword with empty email shows error and does not call repository`() = runTest {
        whenever(authRepository.validateEmail("")).thenReturn("Email address cannot be empty")

        viewModel.forgotPassword()

        val state = viewModel.uiState.value
        assertEquals("Email address cannot be empty", state.emailError)
        assertFalse(state.isLoading)
        assertNull(state.infoMessage)
        verify(authRepository, never()).resetPassword(any())
    }

    @Test
    fun `forgotPassword with malformed email shows format error and does not call repository`() = runTest {
        whenever(authRepository.validateEmail("bad")).thenReturn("Please enter a valid email format")
        viewModel.onEmailChange("bad")

        viewModel.forgotPassword()

        assertEquals("Please enter a valid email format", viewModel.uiState.value.emailError)
        verify(authRepository, never()).resetPassword(any())
    }
}