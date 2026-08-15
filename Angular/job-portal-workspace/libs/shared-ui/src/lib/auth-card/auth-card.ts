import { CommonModule } from '@angular/common';
import { Component, ContentChild, Input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'ui-auth-card',
  imports: [CommonModule, MatCardModule],
  templateUrl: './auth-card.html',
  styleUrl: './auth-card.css',
})
export class AuthCard {
  @Input({required: true}) title: string = '';
  @Input() subTitle: string = '';

  @ContentChild(ReactiveFormsModule) form! : ReactiveFormsModule;
}
