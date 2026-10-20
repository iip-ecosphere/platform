import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ConfigDashboardingModeService {
  dashboardingMode = false;

  constructor() { }
}
