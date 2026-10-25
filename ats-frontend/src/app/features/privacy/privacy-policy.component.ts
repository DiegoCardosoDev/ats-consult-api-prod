import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  PRIVACY_POLICY_CONTACT,
  PRIVACY_POLICY_UPDATED_AT,
  PRIVACY_POLICY_VERSION
} from '../../core/constants/privacy-policy.constants';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './privacy-policy.component.html',
  styleUrl: './privacy-policy.component.scss'
})
export class PrivacyPolicyComponent {
  readonly version = PRIVACY_POLICY_VERSION;
  readonly updatedAt = PRIVACY_POLICY_UPDATED_AT;
  readonly contact = PRIVACY_POLICY_CONTACT;
}
