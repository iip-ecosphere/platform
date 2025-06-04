import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContainersComponent } from './containers.component';
import { MatIconModule } from '@angular/material/icon';

describe('ContainersComponent', () => {
  let component: ContainersComponent;
  let fixture: ComponentFixture<ContainersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ MatIconModule ],
      declarations: [ ContainersComponent ],
      teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContainersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
