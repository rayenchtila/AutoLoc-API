package tn.esprit.rayenbenchtila._ssa1_rayenbenchtila.web.controller;

import java.time.LocalDateTime;

public record ApiError(int statut, String message, LocalDateTime horodatage) {
}
