import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'ui-auth-card',
  imports: [CommonModule],
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.css',
})
export class AuthCard {

  @Input() title: string = '';
  @Input() subTitle: string = '';
}
