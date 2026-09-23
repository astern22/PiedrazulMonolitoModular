import { createRouter, createWebHistory } from 'vue-router'
import { authService } from '@/api'

import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import SpecialtiesView from '@/views/SpecialtiesView.vue'
import AppointmentsView from '@/views/AppointmentsView.vue'
import ProfessionalsView from '@/views/ProfessionalsView.vue'
import SchedulingView from '@/views/SchedulingView.vue'
import PatientsView from '@/views/PatientsView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { title: 'Inicio - Piedrazul' },
    },
    {
      path: '/patients',
      name: 'patients',
      component: PatientsView,
      meta: {
        requiresAuth: true,
        roles: ['PROFESSIONAL', 'MEDICO', 'ADMIN'],
        title: 'Gestión de Pacientes - Piedrazul',
      },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true, title: 'Iniciar Sesion - Piedrazul' },
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresGuest: true, title: 'Registrarse - Piedrazul' },
    },
    {
      path: '/specialties',
      name: 'specialties',
      component: SpecialtiesView,
      meta: {
        requiresAuth: true,
        roles: ['ADMIN', 'SCHEDULER'],
        title: 'Especialidades - Piedrazul',
      },
    },
    {
      path: '/professionals',
      name: 'professionals',
      component: ProfessionalsView,
      meta: {
        requiresAuth: true,
        roles: ['ADMIN', 'SCHEDULER'],
        title: 'Profesionales - Piedrazul',
      },
    },
    {
      path: '/scheduling',
      name: 'scheduling',
      component: SchedulingView,
      meta: {
        requiresAuth: true,
        roles: ['ADMIN', 'SCHEDULER', 'PROFESSIONAL'],
        title: 'Disponibilidad y Horarios - Piedrazul',
      },
    },
    {
      path: '/appointments',
      name: 'appointments',
      component: AppointmentsView,
      meta: { requiresAuth: true, title: 'Citas Medicas - Piedrazul' },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const authenticated = authService.isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    return next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
  }

  if (to.meta.requiresGuest && authenticated) {
    return next({ path: '/' })
  }

  if (to.meta.roles && to.meta.roles.length > 0) {
    const currentUser = authService.getCurrentUser()
    const userRoles = currentUser?.roles || []
    const hasRequiredRole = to.meta.roles.some((role) => userRoles.includes(role))

    if (!hasRequiredRole) {
      return next({ path: '/' })
    }
  }

  next()
})

export default router
