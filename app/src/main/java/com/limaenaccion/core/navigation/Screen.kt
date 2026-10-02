package com.limaenaccion.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Emergency : Screen("emergency")
    object Login : Screen("login")
    object ForgotPassword : Screen("forgot_password")
    object NewPassword : Screen("new_password")

    object RegisterGraph : Screen("register_graph")
    object RegisterStep1 : Screen("register_step1")
    object RegisterStep2 : Screen("register_step2")
    object RegisterStep3 : Screen("register_step3")
    object RegisterVerification : Screen("register_verification")

    object LocationPermission : Screen("location_permission")
    object AddAuxiliaryContact : Screen("add_auxiliary_contact")

    object Main : Screen("main")
    object SosActivation : Screen("sos_activation")
    object SosActive : Screen("sos_active")
    object IncidentDetail : Screen("incident_detail/{incidentId}") {
        fun createRoute(incidentId: String) = "incident_detail/$incidentId"
    }

    object ReportGraph : Screen("report_graph")
    object ReportTypeSelection : Screen("report_type_selection")
    object ReportCamera : Screen("report_camera")
    object ReportPhotoPreview : Screen("report_photo_preview")
    object ReportDetails : Screen("report_details")
    object ReportSuccess : Screen("report_success")

    object EditPersonalData : Screen("profile_edit_personal_data")
    object ChangeProfilePhoto : Screen("profile_change_photo")
    object UpdateAddress : Screen("profile_update_address")
    object ReportHistory : Screen("profile_report_history")
    object ManageContacts : Screen("profile_manage_contacts")
}