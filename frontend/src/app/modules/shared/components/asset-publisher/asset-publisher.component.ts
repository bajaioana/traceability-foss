import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { ImportState, Part } from '@page/parts/model/parts.model';
import { Policy } from '@page/policies/model/policy.model';
import { PolicyService } from '@shared/service/policy.service';
import { Observable, Subscription } from 'rxjs';

@Component({
  selector: 'app-asset-publisher',
  templateUrl: './asset-publisher.component.html',
  styleUrls: ['./asset-publisher.component.scss']
})
export class AssetPublisherComponent {

  @Input() selectedAssets: Part[] = [];
  @Input() isOpen: Observable<boolean>;
  isOpenSubscription: Subscription;

  @Output() submitted = new EventEmitter<void>();

  policiesSubscription: Subscription;
  policiesList: Policy[] = [];
  policyFormControl = new FormControl('', [Validators.required])

  constructor(readonly policyService: PolicyService) {}

  ngOnInit(): void {
    this.isOpenSubscription = this.isOpen.subscribe(next => {
      if(!next) {
        this.policyFormControl.reset();
        return;
      }
        this.getPolicies();
    })
  }

  ngOnDestroy():void {
    if (this.isOpenSubscription) {
      this.isOpenSubscription.unsubscribe();
    }
    if(this.policiesSubscription) {
      this.policiesSubscription.unsubscribe();
    }
  }


  publish() {
    const selectedAssetIds = this.selectedAssets.map(part => part.id)
    this.policyService.publishAssets(selectedAssetIds, this.policyFormControl.value).subscribe(next=> console.log(next));
    this.policyFormControl.reset();
    this.submitted.emit();
  }

  private getPolicies() {
    this.policiesSubscription = this.policyService.getPolicies().subscribe(data => {
      this.policiesList = data;
    })
  }

  checkForNonTransientPart(): boolean {
    return this.selectedAssets.some(part => part.importState !== ImportState.TRANSIENT)
  }
}
