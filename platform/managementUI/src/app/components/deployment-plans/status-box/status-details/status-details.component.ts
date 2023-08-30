import { Component, Input, OnInit, ElementRef, Renderer2, ViewChild } from '@angular/core';
import { statusCollection } from 'src/interfaces';

@Component({
  selector: 'app-status-details',
  templateUrl: './status-details.component.html',
  styleUrls: ['./status-details.component.scss']
})
export class StatusDetailsComponent implements OnInit {

  @Input()  process: statusCollection | undefined;
  @ViewChild('scrollContainer', { static: false })
  scrollContainer!: ElementRef;

  constructor(private renderer: Renderer2) { }

  ngOnInit(): void {
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
}

  scrollToBottom(): void {
    if (this.scrollContainer) {
      const container = this.scrollContainer.nativeElement;
      container.scrollTop = container.scrollHeight;
    }
  }
}
