/********************************************************************************
 * Copyright (c) 2022, 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022, 2023 ZF Friedrichshafen AG
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

import { HttpClient } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { environment } from '@env';

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrls: [ './about.component.scss' ],
})
export class AboutComponent {
  @Input() name: string;
  @Input() repositoryPath: string;
  @Input() license: string;
  @Input() licensePath: string;
  @Input() noticePath: string;
  @Input() sourcePath: string;
  @Input() commitId: string;

  constructor(private http: HttpClient) {
    this.license = 'Apache-2.0';
    this.fetchAppInfo();
  }

  openLink(url: string) {
    window.open(url, '_blank');
  }

  fetchAppInfo() {

    const commitIdNew = environment.gitTag;
    const sourcePathNew = 'https://github.com/eclipse-tractusx/traceability-foss/commit/' + commitIdNew;
    console.log(commitIdNew, 'gittag');
    console.log(sourcePathNew, 'sourcepath');

    this.http.get<any>('/assets/notice/legal-notice.json').subscribe(data => {
      this.sourcePath = data.sourcePath;
      this.commitId = data.commitId;
      this.name = data.name;
      this.repositoryPath = data.repositoryPath;
      this.licensePath = data.licensePath;
      this.noticePath = data.noticePath;
    });
  }

  protected readonly environment = environment;
}
