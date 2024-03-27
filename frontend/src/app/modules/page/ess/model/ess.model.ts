/********************************************************************************
 * Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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
import { PaginationResponse } from '@core/model/pagination.model';

export interface Ess {
  id: string;
  essStatus: string;
  message: string;
  rowNumber: string;
  manufacturerPartId: string;
  nameAtManufacturer: string;
  catenaxSiteId: string;
  bpns: string;
  companyName: string;
  jobId: string;
  status: string;
  impacted: string;
  response: string;
  created: string;
  updated: string;
}

export interface EssResponse {
  id: string;
  essStatus: string;
  message: string;
  rowNumber: string;
  manufacturerPartId: string;
  nameAtManufacturer: string;
  catenaxSiteId: string;
  bpns: string;
  companyName: string;
  jobId: string;
  status: string;
  impacted: string;
  response: string;
  created: string;
  updated: string;
}

export type EssListResponse = PaginationResponse<EssResponse>;

export interface EssFilter {
  id?: string;
  essStatus?: string;
  message?: string;
  rowNumber?: string;
  manufacturerPartId?: string;
  nameAtManufacturer?: string;
  catenaxSiteId?: string;
  bpns?: string;
  companyName?: string;
  jobId?: string;
  status?: string;
  impacted?: string;
  response?: string;
  created?: string;
  updated?: string;
}