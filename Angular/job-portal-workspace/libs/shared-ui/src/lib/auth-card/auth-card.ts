import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'ui-auth-card',
  imports: [CommonModule, MatCardModule],
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.css',
})
export class AuthCard {
  @Input() title: string = '';
  @Input() subTitle: string = '';
}
